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
