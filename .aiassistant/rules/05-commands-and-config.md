---
apply: always
---

## 20. Fill-in project section
- Test command: mvn test
- Lint command: [use the repository’s configured check command, such as mvn spotless:check or mvn checkstyle:check]
- Format command: [use the repository’s configured format command, such as mvn spotless:apply]
- Build command: mvn package
- Do not change without approval: public APIs in api, shared contracts used by other modules, command names, permission nodes, config keys, storage schema, plugin messaging behavior, and documented external behavior