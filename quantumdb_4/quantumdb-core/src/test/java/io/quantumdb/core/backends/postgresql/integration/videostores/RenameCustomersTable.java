package io.quantumdb.core.backends.postgresql.integration.videostores;

import static io.quantumdb.core.backends.postgresql.PostgresTypes.date;
import static io.quantumdb.core.backends.postgresql.PostgresTypes.floats;
import static io.quantumdb.core.backends.postgresql.PostgresTypes.integer;
import static io.quantumdb.core.backends.postgresql.PostgresTypes.varchar;
import static io.quantumdb.core.schema.definitions.Column.Hint.AUTO_INCREMENT;
import static io.quantumdb.core.schema.definitions.Column.Hint.IDENTITY;
import static io.quantumdb.core.schema.definitions.Column.Hint.NOT_NULL;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.quantumdb.core.backends.DatabaseMigrator.MigrationException;
import io.quantumdb.core.schema.definitions.Catalog;
import io.quantumdb.core.schema.definitions.Column;
import io.quantumdb.core.schema.definitions.Table;
import io.quantumdb.core.schema.operations.SchemaOperations;
import io.quantumdb.core.versioning.State;
import io.quantumdb.core.versioning.TableMapping;
import io.quantumdb.core.versioning.Version;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class RenameCustomersTable {

	@ClassRule
	public static PostgresqlBaseScenario setup = new PostgresqlBaseScenario();

	private static State state;
	private static Version origin;
	private static Version target;

	@BeforeClass
	public static void performEvolution() throws SQLException, MigrationException {
		origin = setup.getChangelog().getLastAdded();

		setup.getChangelog().addChangeSet("Michael de Jong",
				SchemaOperations.renameTable("customers", "clients"));

		target = setup.getChangelog().getLastAdded();
		setup.getBackend().persistState(setup.getState());

		setup.getMigrator().migrate(origin.getId(), target.getId());

		state = setup.getBackend().loadState();
	}

	@Test
	public void verifyTableStructure() {
		TableMapping mapping = state.getTableMapping();

		Table stores = new Table(mapping.getTableId(origin, "stores"))
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("name", varchar(255), NOT_NULL))
				.addColumn(new Column("manager_id", integer(), NOT_NULL));

		Table staff = new Table(mapping.getTableId(origin, "staff"))
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("name", varchar(255), NOT_NULL))
				.addColumn(new Column("store_id", integer(), NOT_NULL));

		Table customers = new Table(mapping.getTableId(origin, "customers"))
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("name", varchar(255), NOT_NULL))
				.addColumn(new Column("store_id", integer(), NOT_NULL))
				.addColumn(new Column("referred_by", integer()));

		Table films = new Table(mapping.getTableId(origin, "films"))
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("name", varchar(255), NOT_NULL));

		Table inventory = new Table(mapping.getTableId(origin, "inventory"))
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("store_id", integer(), NOT_NULL))
				.addColumn(new Column("film_id", integer(), NOT_NULL));

		Table paychecks = new Table(mapping.getTableId(origin, "paychecks"))
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("staff_id", integer(), NOT_NULL))
				.addColumn(new Column("date", date(), NOT_NULL))
				.addColumn(new Column("amount", floats(), NOT_NULL));

		Table payments = new Table(mapping.getTableId(origin, "payments"))
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("staff_id", integer()))
				.addColumn(new Column("customer_id", integer(), NOT_NULL))
				.addColumn(new Column("rental_id", integer(), NOT_NULL))
				.addColumn(new Column("date", date(), NOT_NULL))
				.addColumn(new Column("amount", floats(), NOT_NULL));

		Table rentals = new Table(mapping.getTableId(origin, "rentals"))
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("staff_id", integer()))
				.addColumn(new Column("customer_id", integer(), NOT_NULL))
				.addColumn(new Column("inventory_id", integer(), NOT_NULL))
				.addColumn(new Column("date", date(), NOT_NULL));

		stores.addForeignKey("manager_id").referencing(staff, "id");
		staff.addForeignKey("store_id").referencing(stores, "id");
		customers.addForeignKey("referred_by").referencing(customers, "id");
		customers.addForeignKey("store_id").referencing(stores, "id");
		inventory.addForeignKey("store_id").referencing(stores, "id");
		inventory.addForeignKey("film_id").referencing(films, "id");
		paychecks.addForeignKey("staff_id").referencing(staff, "id");
		payments.addForeignKey("staff_id").referencing(staff, "id");
		payments.addForeignKey("customer_id").referencing(customers, "id");
		payments.addForeignKey("rental_id").referencing(rentals, "id");
		rentals.addForeignKey("staff_id").referencing(staff, "id");
		rentals.addForeignKey("customer_id").referencing(customers, "id");
		rentals.addForeignKey("inventory_id").referencing(inventory, "id");

		List<Table> tables = Lists.newArrayList(stores, staff, customers, films, inventory, paychecks, payments, rentals);

		Catalog expected = new Catalog(setup.getCatalogName());
		tables.forEach(expected::addTable);

		assertEquals(expected.getTables(), state.getCatalog().getTables());
	}

	@Test
	public void verifyTableMappings() {
		TableMapping tableMapping = state.getTableMapping();

		Map<String, String> originTableIds = tableMapping.getTableMapping(origin);
		Map<String, String> targetTableIds = tableMapping.getTableMapping(target);

		Map<String, String> expectedOriginTableIds = ImmutableMap.<String, String>builder()
				.put(PostgresqlBaseScenario.STORES_ID, "stores")
				.put(PostgresqlBaseScenario.STAFF_ID, "staff")
				.put(PostgresqlBaseScenario.CUSTOMERS_ID, "customers")
				.put(PostgresqlBaseScenario.FILMS_ID, "films")
				.put(PostgresqlBaseScenario.INVENTORY_ID, "inventory")
				.put(PostgresqlBaseScenario.PAYCHECKS_ID, "paychecks")
				.put(PostgresqlBaseScenario.PAYMENTS_ID, "payments")
				.put(PostgresqlBaseScenario.RENTALS_ID, "rentals")
				.build();

		Map<String, String>expectedTargetTableIds = ImmutableMap.<String, String>builder()
				.put(PostgresqlBaseScenario.STORES_ID, "stores")
				.put(PostgresqlBaseScenario.STAFF_ID, "staff")
				.put(PostgresqlBaseScenario.CUSTOMERS_ID, "clients")
				.put(PostgresqlBaseScenario.FILMS_ID, "films")
				.put(PostgresqlBaseScenario.INVENTORY_ID, "inventory")
				.put(PostgresqlBaseScenario.PAYCHECKS_ID, "paychecks")
				.put(PostgresqlBaseScenario.PAYMENTS_ID, "payments")
				.put(PostgresqlBaseScenario.RENTALS_ID, "rentals")
				.build();

		assertEquals(expectedOriginTableIds, originTableIds);
		assertEquals(expectedTargetTableIds, targetTableIds);
	}

}
