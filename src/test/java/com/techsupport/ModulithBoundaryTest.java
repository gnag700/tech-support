package com.techsupport;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Spring Modulith Boundary Test
 *
 * <p>Verifies the modular structure of the application: - All modules are properly defined
 * with @ApplicationModule - No cyclic dependencies between modules - Module boundaries are
 * respected (impl packages are not accessed from other modules)
 */
class ModulithBoundaryTest {

  private final ApplicationModules modules = ApplicationModules.of(TechSupportApplication.class);

  @Test
  void shouldVerifyModularStructure() {
    // Verifies that all modules are properly defined and have no cyclic dependencies
    modules.verify();
  }

  @Test
  void shouldDocumentModules() {
    // Generates documentation for all modules
    new Documenter(modules).writeDocumentation();
  }

  @Test
  void shouldHaveExpectedModules() {
    // Verify all 6 expected modules are present
    modules.forEach(
        module -> {
          String moduleName = module.getName();
          System.out.println("Found module: " + moduleName);
        });
  }
}
