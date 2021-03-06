package io.quantumdb.core.migration.operations;

import io.quantumdb.core.schema.definitions.Catalog;
import io.quantumdb.core.schema.definitions.Index;
import io.quantumdb.core.schema.definitions.Table;
import io.quantumdb.core.schema.operations.CreateIndex;
import io.quantumdb.core.state.RefLog;
import io.quantumdb.core.state.RefLog.TableRef;
import io.quantumdb.core.versioning.Version;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
class CreateIndexMigrator implements SchemaOperationMigrator<CreateIndex> {

	@Override
	public void migrate(Catalog catalog, RefLog refLog, Version version, CreateIndex operation) {
		String tableName = operation.getTableName();
		TransitiveTableMirrorer.mirror(catalog, refLog, version, tableName);

		TableRef tableRef = refLog.getTableRef(version, tableName);
		String tableId = tableRef.getTableId();
		Table table = catalog.getTable(tableId);
		table.addIndex(new Index(operation.getColumns(), operation.isUnique()));
	}

}
