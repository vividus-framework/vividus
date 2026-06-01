# GitHub Copilot instructions for VIVIDUS

## Build & quality validation

Before committing any Java changes, run the full quality suite for every affected module
(replace `<module>` with the actual Gradle project name, e.g. `vividus-plugin-rest-api`):

    # Ensure JAVA_HOME points to Java 21 (see README.md)
    ./gradlew \
      :<module>:checkstyleMain :<module>:checkstyleTest \
      :<module>:spotbugsMain :<module>:spotbugsTest \
      :<module>:pmdMain :<module>:pmdTest \
      :<module>:test

Fix **all** violations before pushing.

## Files never to commit

The following files **must not** be staged or committed:

- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`
- Any `.gradle/` build output directory

These are either managed by the `vividus-build-system` submodule or are build artefacts. If accidentally staged, remove them with `git rm -r --cached <path>`.

## Qodana patterns

### Optional as field or parameter (QDJVM S3553)

**Never** use `Optional<T>` as a field type or constructor/method parameter. Use `@org.jspecify.annotations.Nullable T` instead. Call `Optional.ofNullable(value)` only at the use site when an `Optional` return type is required by the called API.

```java
// Wrong
private Optional<String> jiraInstanceKey;

// Correct
private @Nullable String jiraInstanceKey;
```

## JUnit assertions and annotations

- Prefer `assertTrue(condition)` / `assertFalse(condition)` over `assertEquals(true, condition)` / `assertEquals(false, condition)`.
- Use `org.junit.jupiter.api.util.SetSystemProperty` / `ClearSystemProperty` (JUnit 6.1 native). Keep JUnit Pioneer (`@SetSystemProperty`, `@ClearSystemProperty`) only where `StdIo` or `SetEnvironmentVariable` is required.

## General code style

- **No duplicated logic.** Extract shared code into a private or protected method rather than copy-pasting.
- Prefer concise expressions; avoid unnecessary local variables.
- All public API classes and step methods must have Javadoc.

## Documentation

Documentation pages live under `docs/modules/ROOT/pages/` in AsciiDoc format. When adding a new feature, add or update the corresponding `.adoc` file in the same PR.
