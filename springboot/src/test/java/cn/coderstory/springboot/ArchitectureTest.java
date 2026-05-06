package cn.coderstory.springboot;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * 架构规则测试 — 确保分层依赖关系正确。
 * 当前部分规则因已知违规暂时禁用，将在 Phase 18 修复后启用。
 */
class ArchitectureTest {

    @Disabled("Phase 18 修复 Controller→Mapper 直接注入后启用")
    @Test
    void controllerShouldNotDependOnMapper() {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage("..controller..")
            .should().dependOnClassesThat().resideInAnyPackage("..mapper..")
            .because("Controller 不应直接依赖 Mapper，应通过 Service 层访问");
        rule.check(new ClassFileImporter().importPackages("cn.coderstory.springboot"));
    }

    @Disabled("Phase 18 修复分层依赖后启用")
    @Test
    void serviceShouldNotDependOnController() {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage("..service..")
            .should().dependOnClassesThat().resideInAnyPackage("..controller..")
            .because("Service 层不应反向依赖 Controller 层");
        rule.check(new ClassFileImporter().importPackages("cn.coderstory.springboot"));
    }
}
