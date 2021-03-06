package io.quantumdb.core.migration;

import static com.google.common.base.Preconditions.checkState;

import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.quantumdb.core.backends.Backend;
import io.quantumdb.core.backends.DatabaseMigrator;
import io.quantumdb.core.migration.utils.VersionTraverser;
import io.quantumdb.core.versioning.Changelog;
import io.quantumdb.core.versioning.State;
import io.quantumdb.core.versioning.TableMapping;
import io.quantumdb.core.versioning.Version;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Migrator {

	private final Backend backend;

	@Inject
	Migrator(Backend backend) {
		this.backend = backend;
	}

	public void migrate(String sourceVersionId, String targetVersionId) throws DatabaseMigrator.MigrationException {
		log.info("Migrating database structure from version: {} to version: {}", sourceVersionId, targetVersionId);

		State state = loadState();
		Changelog changelog = state.getChangelog();
		TableMapping tableMapping = state.getTableMapping();
		Set<Version> versions = tableMapping.getVersions();

		Set<String> origins = Sets.newHashSet(changelog.getRoot().getId());
		if (!versions.isEmpty()) {
			origins = versions.stream()
					.map(Version::getId)
					.collect(Collectors.toSet());
		}

		if (!origins.contains(sourceVersionId)) {
			log.warn("Not migrating database structure -> Not currently at version: {}", sourceVersionId);
			return;
		}

		if (origins.contains(targetVersionId)) {
			log.warn("Not migrating database structure -> Already at version: {}", targetVersionId);
			return;
		}

		Version from = changelog.getVersion(sourceVersionId);
		Version to = changelog.getVersion(targetVersionId);

		verifyPathAndState(state, from, to);

		backend.getMigrator().migrate(state, from, to);
	}

	private State loadState() throws DatabaseMigrator.MigrationException {
		try {
			return backend.loadState();
		}
		catch (SQLException e) {
			throw new DatabaseMigrator.MigrationException("Could not load current state: " + e.getMessage(), e);
		}
	}

	private void verifyPathAndState(State state, Version from, Version to) {
		TableMapping tableMapping = state.getTableMapping();
		Set<Version> versions = tableMapping.getVersions();

		if (!versions.isEmpty()) {
			boolean atCorrectVersion = versions.contains(from);
			checkState(atCorrectVersion, "The database is not at version: '" + from + "' but at: '" + versions + "'.");
		}
		else {
			checkState(from.isRoot(), "Database is not initialized yet, you must start migrating from the root node.");
		}

		VersionTraverser.findChildPath(from, to)
				.orElseThrow(() -> new IllegalStateException("No path from " + from.getId() + " to " + to.getId()));
	}

}
