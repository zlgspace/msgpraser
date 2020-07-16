package com.zlgspace.msgpraser;


import com.google.auto.service.AutoService;
import com.zlgspace.msgpraser.annotation.BindEntity;
import com.zlgspace.msgpraser.annotation.CallbackInterface;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class MessageParserProcessor extends AbstractProcessor {

    private Elements elementUtils;

    private Filer mFiler;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(CallbackInterface.class.getCanonicalName());
        set.add(BindEntity.class.getCanonicalName());
        return set;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(CallbackInterface.class);
        for(Element e:elements){
            analysisAnnotated((TypeElement)e);
        }
        return false;
    }

    /**
     * 生成java文件
     * @param classElement 注解
     */
    private void analysisAnnotated(TypeElement classElement) {
        CallbackInterface annotation = classElement.getAnnotation(CallbackInterface.class);
        String clazzSimpleName = classElement.getSimpleName().toString();
        String clazzFullName = classElement.getQualifiedName().toString();
        String pkgName = elementUtils.getPackageOf(classElement).toString();


        print("clazzSimpleName="+clazzSimpleName);
        print("clazzFullName="+clazzFullName);
        print("pkgName="+pkgName);

        List<? extends Element> fieldList = elementUtils.getAllMembers(classElement);

        String newClzName = clazzSimpleName+"_Callback";

        StringBuilder importPkgBuilder = new StringBuilder();
        StringBuilder methodBuilder = new StringBuilder();
        StringBuilder fieldBuilder = new StringBuilder();


        importPkgBuilder.append("package "+pkgName+";\n");



        for(Element e:fieldList){

//            Annotation bindEntity = e.getAnnotation(BindEntity.class);
            List<? extends AnnotationMirror> annotationMirrorList =  e.getAnnotationMirrors();
            AnnotationMirror bindEntityMirror = null;
            for(AnnotationMirror item:annotationMirrorList){
                if(BindEntity.class.getName().equals(item.getAnnotationType().toString())){
                    bindEntityMirror = item;
                    break;
                }
            }

            if(bindEntityMirror == null)
                continue;

            Map<? extends ExecutableElement, ? extends AnnotationValue> bindEntityValues =  bindEntityMirror.getElementValues();

            ExecutableElement bindEntityExecutableElement = null;

            for(ExecutableElement key : bindEntityValues.keySet()){
                bindEntityExecutableElement = key;
                break;
            }

            String fieldName = e.getSimpleName().toString();

            if(bindEntityExecutableElement == null){
                methodBuilder.append("void " + fieldName + "();\n");
            }else {
                String bindEntityValueStr = bindEntityValues.get(bindEntityExecutableElement).toString();

                if (bindEntityValueStr == null)
                    continue;


                String entityClzFullName = bindEntityValueStr.replace(".class", "");

                String entityClzSimpleName = entityClzFullName.substring(entityClzFullName.lastIndexOf(".") + 1, entityClzFullName.length());

                if ("None".equals(entityClzSimpleName)) {
                    entityClzSimpleName = "";
                }
//
                fieldBuilder.append("String " + fieldName + " = \"" + fieldName + "\";\n");

                if (entityClzSimpleName != null && !entityClzSimpleName.isEmpty()) {
                    String impt = "import " + entityClzFullName + ";\n";
                    if(importPkgBuilder.indexOf(impt)==-1)//每个包只导入一次
                        importPkgBuilder.append(impt);
                    methodBuilder.append("void " + fieldName + "(" + entityClzSimpleName + " arg);\n");
                } else {
                    methodBuilder.append("void " + fieldName + "();\n");
                }
            }
        }

        StringBuilder classBuilder = new StringBuilder();
        classBuilder.append("public interface "+newClzName+"{\n");
        classBuilder.append(fieldBuilder);
        classBuilder.append(methodBuilder);
        classBuilder.append("}\n");

        importPkgBuilder.append(classBuilder);

        try { // write the file
            JavaFileObject source = mFiler.createSourceFile(pkgName +"."+ newClzName);
            Writer writer = source.openWriter();
            writer.write(importPkgBuilder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // Note: calling e.printStackTrace() will print IO errors
            // that occur from the file already existing after its first run, this is normal
        }

    }


    private void print(String msg){
        System.out.println("MessageParserProcessor:"+msg);
    }
}
