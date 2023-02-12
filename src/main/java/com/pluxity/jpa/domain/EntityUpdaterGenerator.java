package com.pluxity.jpa.domain;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("EntityUpdate")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class EntityUpdaterGenerator extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotation)) {
                TypeElement entityType = (TypeElement) annotatedElement;
                String entityName = entityType.getSimpleName().toString();
                String updaterClassName = entityName + "Updater";
                String updaterClassSource = generateUpdaterSource(entityType);

                try {
                    JavaFileObject updaterFile = processingEnv.getFiler().createSourceFile(updaterClassName);
                    Writer writer = updaterFile.openWriter();
                    writer.write(updaterClassSource);
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    private String generateUpdaterSource(TypeElement entityType) {
        String entityName = entityType.getSimpleName().toString();
        String updaterName = entityName + "Updater";

        StringBuilder updaterSource = new StringBuilder();
        updaterSource.append("package com.pluxity.jpa.domain;\n\n");
        updaterSource.append("import java.util.Map;\n\n");
        updaterSource.append("public class " + updaterName + " implements EntityUpdater<" + entityName + "> {\n");
        updaterSource.append("    @Override\n");
        updaterSource.append("    public void update(" + entityName + " entity, Map<String, Object> updates) {\n");

        for (Element field : entityType.getEnclosedElements()) {
            if (field.getKind().isField()) {
                String fieldName = field.getSimpleName().toString();
                updaterSource.append("        Object value = updates.get(\"" + fieldName + "\");\n");
                updaterSource.append("        if (value != null) {\n");
                updaterSource.append("            entity." + fieldName + " = (" + field.asType().toString() + ") value;\n");
                updaterSource.append("        }\n");
            }
        }
        updaterSource.append("    }\n");
        updaterSource.append("}\n");

        return updaterSource.toString();
    }
}
