package io.quantumdb.core.backends.postgresql.integration.videostores;

import static io.quantumdb.core.backends.postgresql.PostgresTypes.date;
import static io.quantumdb.core.backends.postgresql.PostgresTypes.floats;
import static io.quantumdb.core.backends.postgresql.PostgresTypes.integer;
import static io.quantumdb.core.backends.postgresql.PostgresTypes.varchar;
import static io.quantumdb.core.backends.postgresql.integration.videostores.PostgresqlBaseScenario.*;
import static io.quantumdb.core.schema.definitions.Column.Hint.AUTO_INCREMENT;
import static io.quantumdb.core.schema.definitions.Column.Hint.IDENTITY;
import static io.quantumdb.core.schema.definitions.Column.Hint.NOT_NULL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.sql.SQLException;
import java.util.List;

import com.google.common.collect.Lists;
import io.quantumdb.core.backends.DatabaseMigrator.MigrationException;
import io.quantumdb.core.schema.definitions.Catalog;
import io.quantumdb.core.schema.definitions.Column;
import io.quantumdb.core.schema.definitions.Table;
import io.quantumdb.core.schema.operations.SchemaOperations;
import io.quantumdb.core.versioning.RefLog;
import io.quantumdb.core.versioning.State;
import io.quantumdb.core.versioning.Version;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class AddColumnToCustomersTable {

	@ClassRule
	public static PostgresqlBaseScenario setup = new PostgresqlBaseScenario();

	private static State state;
	private static Version origin;
	private static Version target;

	@BeforeClass
	public static void performEvolution() throws SQLException, MigrationException {
		setup.insertTestData();

		origin = setup.getChangelog().getLastAdded();

		setup.getChangelog().addChangeSet("Michael de Jong",
				SchemaOperations.addColumn("customers", "date_of_birth", date()));

		target = setup.getChangelog().getLastAdded();
		setup.getBackend().persistState(setup.getState());

		setup.getMigrator().migrate(origin.getId(), target.getId());

		state = setup.getBackend().loadState();
	}

	@Test
	public void verifyTableStructure() {
		RefLog refLog = state.getRefLog();

		// Original tables and foreign keys.

		Table stores = new Table(refLog.getTableRef(origin, "stores").getTableId())
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("name", varchar(255), NOT_NULL))
				.addColumn(new Column("manager_id", integer(), NOT_NULL));

		Table staff = new Table(refLog.getTableRef(origin, "staff").getTableId())
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("name", varchar(255), NOT_NULL))
				.addColumn(new Column("store_id", integer(), NOT_NULL));

		Table customers = new Table(refLog.getTableRef(origin, "customers").getTableId())
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("name", varchar(255), NOT_NULL))
				.addColumn(new Column("store_id", integer(), NOT_NULL))
				.addColumn(new Column("referred_by", integer()));

		Table films = new Table(refLog.getTableRef(origin, "films").getTableId())
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("name", varchar(255), NOT_NULL));

		Table inventory = new Table(refLog.getTableRef(origin, "inventory").getTableId())
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("store_id", integer(), NOT_NULL))
				.addColumn(new Column("film_id", integer(), NOT_NULL));

		Table paychecks = new Table(refLog.getTableRef(origin, "paychecks").getTableId())
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("staff_id", integer(), NOT_NULL))
				.addColumn(new Column("date", date(), NOT_NULL))
				.addColumn(new Column("amount", floats(), NOT_NULL));

		Table payments = new Table(refLog.getTableRef(origin, "payments").getTableId())
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("staff_id", integer()))
				.addColumn(new Column("customer_id", integer(), NOT_NULL))
				.addColumn(new Column("rental_id", integer(), NOT_NULL))
				.addColumn(new Column("date", date(), NOT_NULL))
				.addColumn(new Column("amount", floats(), NOT_NULL));

		Table rentals = new Table(refLog.getTableRef(origin, "rentals").getTableId())
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

		// New tables and foreign keys.

		Table newStores = new Table(refLog.getTableRef(target, "stores").getTableId())
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("name", varchar(255), NOT_NULL))
				.addColumn(new Column("manager_id", integer(), NOT_NULL));

		Table newStaff = new Table(refLog.getTableRef(target, "staff").getTableId())
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("name", varchar(255), NOT_NULL))
				.addColumn(new Column("store_id", integer(), NOT_NULL));

		Table newInventory = new Table(refLog.getTableRef(target, "inventory").getTableId())
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("store_id", integer(), NOT_NULL))
				.addColumn(new Column("film_id", integer(), NOT_NULL));

		Table newPaychecks = new Table(refLog.getTableRef(target, "paychecks").getTableId())
				.addColumn(new Column("id", integer(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("staff_id", integer(), NOT_NULL))
				.addColumn(new Column("date", date(), NOT_NULL))
				.addColumn(new Column("amount", floats(), NOT_NULL));

		Table newCustomers = new Table(refLog.getTableRef(target, "customers").getTableId())
				.addColumn(new Column("id", integer(), customers.getColumn("id").getSequence(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("name", varchar(255), NOT_NULL))
				.addColumn(new Column("store_id", integer(), NOT_NULL))
				.addColumn(new Column("referred_by", integer()))
				.addColumn(new Column("date_of_birth", date()));

		Table newPayments = new Table(refLog.getTableRef(target, "payments").getTableId())
				.addColumn(new Column("id", integer(), payments.getColumn("id").getSequence(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("staff_id", integer()))
				.addColumn(new Column("customer_id", integer(), NOT_NULL))
				.addColumn(new Column("rental_id", integer(), NOT_NULL))
				.addColumn(new Column("date", date(), NOT_NULL))
				.addColumn(new Column("amount", floats(), NOT_NULL));

		Table newRentals = new Table(refLog.getTableRef(target, "rentals").getTableId())
				.addColumn(new Column("id", integer(), rentals.getColumn("id").getSequence(), IDENTITY, AUTO_INCREMENT, NOT_NULL))
				.addColumn(new Column("staff_id", integer()))
				.addColumn(new Column("customer_id", integer(), NOT_NULL))
				.addColumn(new Column("inventory_id", integer(), NOT_NULL))
				.addColumn(new Column("date", date(), NOT_NULL));


		newStores.addForeignKey("manager_id").referencing(newStaff, "id");
		newStaff.addForeignKey("store_id").referencing(newStores, "id");
		newInventory.addForeignKey("store_id").referencing(newStores, "id");
		newInventory.addForeignKey("film_id").referencing(films, "id");
		newPaychecks.addForeignKey("staff_id").referencing(newStaff, "id");
		newCustomers.addForeignKey("referred_by").referencing(newCustomers, "id");
		newCustomers.addForeignKey("store_id").referencing(newStores, "id");
		newPayments.addForeignKey("staff_id").referencing(newStaff, "id");
		newPayments.addForeignKey("customer_id").referencing(newCustomers, "id");
		newPayments.addForeignKey("rental_id").referencing(newRentals, "id");
		newRentals.addForeignKey("staff_id").referencing(newStaff, "id");
		newRentals.addForeignKey("customer_id").referencing(newCustomers, "id");
		newRentals.addForeignKey("inventory_id").referencing(inventory, "id");

		List<Table> tables = Lists.newArrayList(stores, staff, customers, films, inventory, paychecks, payments, rentals,
				newCustomers, newPayments, newRentals, newStores, newStaff, newInventory, newPaychecks);

		Catalog expected = new Catalog(setup.getCatalogName());
		tables.forEach(expected::addTable);

		assertEquals(expected.getTables(), state.getCatalog().getTables());
	}

	@Test
	public void verifyTableMappings() {
		RefLog refLog = state.getRefLog();

		// Unchanged tables
		assertEquals(FILMS_ID, refLog.getTableRef(target, "films").getTableId());

		// Ghosted tables
		assertNotEquals(STORES_ID, refLog.getTableRef(target, "stores").getTableId());
		assertNotEquals(STAFF_ID, refLog.getTableRef(target, "staff").getTableId());
		assertNotEquals(INVENTORY_ID, refLog.getTableRef(target, "inventory").getTableId());
		assertNotEquals(PAYCHECKS_ID, refLog.getTableRef(target, "paychecks").getTableId());
		assertNotEquals(CUSTOMERS_ID, refLog.getTableRef(target, "customers").getTableId());
		assertNotEquals(PAYMENTS_ID, refLog.getTableRef(target, "payments").getTableId());
		assertNotEquals(RENTALS_ID, refLog.getTableRef(target, "rentals").getTableId());
	}

}
