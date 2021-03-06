package io.quantumdb.core.migration.operations;

import io.quantumdb.core.migration.utils.DataMappings;
import io.quantumdb.core.schema.definitions.Catalog;
import io.quantumdb.core.schema.operations.SchemaOperation;
import io.quantumdb.core.versioning.TableMapping;
import io.quantumdb.core.versioning.Version;

interface SchemaOperationMigrator<T extends SchemaOperation> {

	void migrate(Catalog catalog, TableMapping tableMapping, DataMappings dataMappings, Version version, T operation);

}
