package com.pluxity.jpa.domain;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
public class UpdateFieldProcessor extends AbstractProcessor{

    private static final String GENERATED_CLASS_NAME_SUFFIX = "Update";

    private Elements elementUtils;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv){
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(UpdateField.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(UpdateField.class)) {
            if(element instanceof VariableElement variableElement){
                String fieldName = variableElement.getSimpleName().toString();
                TypeElement classElement = (TypeElement) variableElement.getEnclosingElement();

                if (classElement.getInterfaces().stream().anyMatch(type -> isAssignable(type, Updatable.class))){
                    try{
                        generateUpdateClass(classElement, fieldName);
                    }catch(IOException e){
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate update class: " + e.getMessage(), variableElement);
                    }
                }else{
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Class " + classElement.getSimpleName() + " does not implement Updatable", variableElement);
                }
            }
        }
        return true;
    }

    private boolean isAssignable(TypeMirror typeMirror, Class<?> clazz) {
        TypeMirror classType = elementUtils.getTypeElement(clazz.getCanonicalName()).asType();
        return typeUtils.isAssignable(typeMirror, classType);
    }

    private void generateUpdateClass(TypeElement classElement, String fieldName) throws IOException {
        String packageName = elementUtils.getPackageOf(classElement).getQualifiedName().toString();
        String className = classElement.getSimpleName().toString();
        try(Writer writer = processingEnv.getFiler().createSourceFile(packageName + "." + className + GENERATED_CLASS_NAME_SUFFIX).openWriter()){
            writer.write("package " + packageName + ";\n\n");
            writer.write("import " + classElement.getQualifiedName() + ";\n");
            writer.write("import " + classElement.getQualifiedName() + ";\n");
            writer.write("public class " + className + GENERATED_CLASS_NAME_SUFFIX + " {\n");
            writer.write("    public static void update(" + className + " entity, " + classElement.getInterfaces().get(0) + " request) {\n");

            for (Element element : classElement.getEnclosedElements()) {
                if(element instanceof VariableElement variableElement){
                    if (variableElement.getAnnotation(UpdateField.class) != null){
                        String fieldNameElement = variableElement.getSimpleName().toString();
                        String setterName = getSetterName(fieldNameElement);
                        writer.write(" if (request.get" + setterName + "() != null) {\n");
                        writer.write(" entity." + fieldNameElement + " = request.get" + setterName + "();\n");
                        writer.write(" }\n");
                    }
                }
            }

            writer.write("    }\n");

        }
    }

    private String getSetterName(String fieldName) {
        return Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }
}
