package io.quantumdb.core.schema.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.collect.Sets;
import io.quantumdb.core.schema.definitions.Column;
import io.quantumdb.core.schema.definitions.PostgresTypes;
import org.junit.Test;

public class AlterColumnTest {

	@Test
	public void testRenamingColumn() {
		AlterColumn operation = SchemaOperations.alterColumn("users", "name")
				.rename("full_name");

		assertEquals("users", operation.getTableName());
		assertEquals("name", operation.getColumnName());
		assertEquals("full_name", operation.getNewColumnName().get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRenamingThrowsExceptionWhenInputIsNull() {
		SchemaOperations.alterColumn("users", "name")
				.rename(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRenamingThrowsExceptionWhenInputIsEmptyString() {
		SchemaOperations.alterColumn("users", "name")
				.rename("");
	}

	@Test
	public void testModifyingDataType() {
		AlterColumn operation = SchemaOperations.alterColumn("users", "name")
				.modifyDataType(PostgresTypes.varchar(255));

		assertEquals(PostgresTypes.varchar(255), operation.getNewColumnType().get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyingDataTypeThrowsExceptionWhenInputIsNull() {
		SchemaOperations.alterColumn("users", "name")
				.modifyDataType(null);
	}

	@Test
	public void testModifyingDefaultExpression() {
		AlterColumn operation = SchemaOperations.alterColumn("users", "name")
				.modifyDefaultExpression("'unknown'");

		assertEquals("'unknown'", operation.getNewDefaultValueExpression().get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyingDefaultExpressionThrowsExceptionWhenInputIsNull() {
		SchemaOperations.alterColumn("users", "name")
				.modifyDefaultExpression(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testModifyingDefaultExpressionThrowsExceptionWhenInputIsEmptyString() {
		SchemaOperations.alterColumn("users", "name")
				.modifyDefaultExpression("");
	}

	@Test
	public void testDroppingDefaultExpression() {
		AlterColumn operation = SchemaOperations.alterColumn("users", "name")
				.dropDefaultExpression();

		assertFalse(operation.getNewDefaultValueExpression().isPresent());
	}

	@Test
	public void testAddingHint() {
		AlterColumn operation = SchemaOperations.alterColumn("users", "name")
				.addHint(Column.Hint.NOT_NULL);

		assertEquals(Sets.newHashSet(Column.Hint.NOT_NULL), operation.getHintsToAdd());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddingHintThrowsExceptionWhenInputIsNull() {
		SchemaOperations.alterColumn("users", "name")
				.addHint(null);
	}

	@Test
	public void testDroppingHint() {
		AlterColumn operation = SchemaOperations.alterColumn("users", "name")
				.dropHint(Column.Hint.NOT_NULL);

		assertEquals(Sets.newHashSet(Column.Hint.NOT_NULL), operation.getHintsToDrop());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDroppingHintThrowsExceptionWhenInputIsNull() {
		SchemaOperations.alterColumn("users", "name")
				.dropHint(null);
	}

}
