# PermissionsEx 1.23.5 public API baseline
#
# Built from git tag STABLE-1.23.5 (final stable classic API).
# Classes-only JAR (no shaded deps) used by PublicApiBinaryContractTest.
#
# Rebuild (from repo root):
#   git worktree add ../pex-stable-1.23.5 STABLE-1.23.5
#   mvn -f ../pex-stable-1.23.5/pom.xml -Pmc-1.8.8 -DskipTests package
#   jar --create --file=PermissionsExApiAdapter/src/test/resources/baselines/PermissionsEx-1.23.5-api.jar \
#       -C ../pex-stable-1.23.5/target/classes .
#   mvn -pl PermissionsExApiAdapter test -Dtest=PublicApiBinaryContractTest -Dpex.contracts.update=true

# PermissionsEx 1.23.4 public API baseline (PermissionsEx-1.23.4-api.jar)
#
# What plugins of the 1.23.4 era compiled against, used by Legacy1234BinaryContractTest
# and to compile scripts/legacy-plugins/probe. 1.23.5 removed members those plugins use,
# such as the static PermissionsEx.getUser(Player).
#
# Built from git tag STABLE-1.23.4, for Java 8. Three of its dependencies are no longer
# published (Mojang's AccountsClient, NetEvents, Gravity's Updater), so it is compiled
# with compile-only stand-ins (scripts/pex-baseline/stubs), which stay in the jar so
# reflection can resolve private members. Rebuild (from repo root):
#   scripts/pex-baseline/build-api.sh STABLE-1.23.4
