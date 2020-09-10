package com.zlgspace.msgpraser.annotation;


import com.google.auto.service.AutoService;
import com.zlgspace.apt.base.BuildClass;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;


@AutoService(Processor.class)
public class MessageParserProcessor extends AbstractProcessor {

    private Elements elementUtils;

    private Filer mFiler;

    private  Map<String,MsgIdHandler> msgMap = new HashMap<>();

    private  HashMap<String,String> mtbSwitchMap = new HashMap<>();

    private  HashMap<String,ArrayList<String>> msgIdsArray = new HashMap<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        elementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(MessageDescription.class.getCanonicalName());
        set.add(BindEntity.class.getCanonicalName());
        set.add(CallbackMethod.class.getCanonicalName());
        return set;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(MessageDescription.class);
        for(Element e:elements){
            mkMsgInterface((TypeElement)e);
        }

        Set<? extends Element> callbackMethod = roundEnvironment.getElementsAnnotatedWith(CallbackMethod.class);
        HashMap<String , BuildClass> clzMap = new HashMap<>();
        for(Element e:callbackMethod){
            mkMsgTargetBroker(e,clzMap);
        }

        for(String msgId:clzMap.keySet()){
            BuildClass bc = clzMap.get(msgId);
            bc.appendImport("import com.zlgspace.msgpraser.MsgIdDescription;\n");
            bc.appendImport("import com.zlgspace.msgpraser.MsgTargetBroker;\n");
            String switchStr = mtbSwitchMap.get(bc.getName());
            switchStr = switchStr+"}\n";
            bc.appendMethod(" @Override\npublic boolean dispatch(String msgId, Object body) {\n");
            bc.appendMethod("boolean intercept = false;\n");
            bc.appendMethod(switchStr);
            bc.appendMethod("return intercept;\n");
            bc.appendMethod("}\n");

            ArrayList<String> msgIds = msgIdsArray.get(bc.getName());
            String targetName = bc.getName().split("_")[0];

            bc.appendMethod("public "+bc.getName()+"("+targetName+" target){\n");
            bc.appendMethod("obj = target;\n");
            if(msgIds==null||msgIds.isEmpty()){
                bc.appendMethod("rcvMsgIds = new MsgIdDescription[0];\n");
            }else {
                bc.appendMethod("rcvMsgIds = new MsgIdDescription["+msgIds.size()+"];\n");
                for (int i = 0; i < msgIds.size(); i++) {
                    MsgIdHandler handler = msgMap.get(msgIds.get(i));
                    bc.appendMethod("MsgIdDescription mid"+i+" = new MsgIdDescription();\n");
                    bc.appendMethod("mid"+i+".setMsgId(\""+handler.msgId+"\");\n");
                    bc.appendMethod("mid"+i+".setHasParams("+handler.hasParams()+");\n");
                    if(handler.hasParams())
                        bc.appendMethod("mid"+i+".setBindEntity("+handler.msgParams.get(0)+".class);\n");
                    bc.appendMethod("rcvMsgIds["+i+"] = mid"+i+";\n");
                }
            }
            bc.appendMethod("}");

            try { // write the file
                JavaFileObject source = mFiler.createSourceFile(bc.getPackage() +"."+ bc.getName());
                Writer writer = source.openWriter();
                writer.write(bc.toString());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                // Note: calling e.printStackTrace() will print IO errors
                // that occur from the file already existing after its first run, this is normal
            }
        }
        return false;
    }

    private void mkMsgInterface(TypeElement classElement) {
        MessageDescription annotation = classElement.getAnnotation(MessageDescription.class);
        String clazzSimpleName = classElement.getSimpleName().toString();
        String clazzFullName = classElement.getQualifiedName().toString();
        String pkgName = elementUtils.getPackageOf(classElement).toString();

        List<? extends Element> fieldList = elementUtils.getAllMembers(classElement);

        String newClzName = clazzSimpleName+"_Callback";

        BuildClass callbackInterface = new BuildClass();
        callbackInterface.setPackage(pkgName);
        callbackInterface.setClzModifiers("public interface ");
        callbackInterface.setName(newClzName);

//        BuildClass msgDispatcher = new BuildClass();
//        msgDispatcher.setPackage(pkgName);
//        msgDispatcher.setClzModifiers("class ");
//        msgDispatcher.setName(NAME_MSG_DISPACHER);
//
//        msgDispatcher.appendImport("import java.util.ArrayList;\n");
//        msgDispatcher.appendImport("import java.util.HashMap;\n");
//        msgDispatcher.appendImport("import com.zlgspace.msgpraser.MsgTargetBroker;\n");
//
//        String msgDispatcher_BindMsgMap = "bindMsgMap";
//        msgDispatcher.appendField(" private static final Object lock = new Object();\n");
//        msgDispatcher.appendField("private HashMap<String, ArrayList<MsgTargetBroker>> "+msgDispatcher_BindMsgMap+" = new HashMap<>();\n");
//
//        msgDispatcher.appendMethod("public void bind(MsgTargetBroker tb){\n");
//        msgDispatcher.appendMethod("MsgIdDescription msgIds[] = mtb.getRcvMsgIds();\n");
//        msgDispatcher.appendMethod("if(msgIds==null||msgIds.length==0) return;\n");
//        msgDispatcher.appendMethod("synchronized (lock){\n");
//        msgDispatcher.appendMethod("for(MsgIdDescription msgId:msgIds){\n");
//        msgDispatcher.appendMethod("String id = msgId.getMsgId();\n");
//        msgDispatcher.appendMethod("ArrayList<MsgTargetBroker> list = null;\n");
//        msgDispatcher.appendMethod("if(!"+msgDispatcher_BindMsgMap+".containsKey(id))\n");
//        msgDispatcher.appendMethod( msgDispatcher_BindMsgMap+".put(id,new ArrayList<MsgTargetBroker>());\n");
//        msgDispatcher.appendMethod("ArrayList<MsgTargetBroker> list = "+msgDispatcher_BindMsgMap+".get(id);\n");
//        msgDispatcher.appendMethod("if(list.contains(tb)) continue;\n");
//        msgDispatcher.appendMethod("list.add(tb);\n");
//        msgDispatcher.appendMethod("}\n");
//        msgDispatcher.appendMethod("}\n");
//        msgDispatcher.appendMethod("}\n");
//
//
//        msgDispatcher.appendMethod("public void unbind(MsgTargetBroker tb){\n");
//        msgDispatcher.appendMethod("MsgIdDescription msgIds[] = mtb.getRcvMsgIds();\n");
//        msgDispatcher.appendMethod("if(msgIds==null||msgIds.length==0) return;\n");
//        msgDispatcher.appendMethod("synchronized (lock){\n");
//        msgDispatcher.appendMethod("for(MsgIdDescription msgIdD:msgIds){\n");
//        msgDispatcher.appendMethod("String id = msgIdD.getMsgId();\n");
//        msgDispatcher.appendMethod("if(!bindMsgTargetMap.containsKey(id)) continue;\n");
//        msgDispatcher.appendMethod("bindMsgTargetMap.get(id).remove(mtb);\n");
//        msgDispatcher.appendMethod("}\n");
//        msgDispatcher.appendMethod("}\n");
//        msgDispatcher.appendMethod("}\n");



//        StringBuilder dispathcerMethod = new StringBuilder();
//        dispathcerMethod.append("public void dispatchMsg(String msgId,Object obj){\n");

        for(Element e:fieldList){
            BindEntity bindEntity = e.getAnnotation(BindEntity.class);

            if(bindEntity==null){
                continue;
            }

            if(e.getKind()!=ElementKind.ENUM_CONSTANT){
                continue;
            }

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
            msgMap.put(fieldName,new MsgIdHandler(fieldName));
            if(bindEntityExecutableElement == null){
                callbackInterface.appendMethod("void " + fieldName + "();\n");
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
                callbackInterface.appendField("String " + fieldName + " = \"" + fieldName + "\";\n");

                if (entityClzSimpleName != null && !entityClzSimpleName.isEmpty()) {
                    String impt = "import " + entityClzFullName + ";\n";
                    callbackInterface.appendImport(impt);
                    callbackInterface.appendMethod("void " + fieldName + "(" + entityClzSimpleName + " arg);\n");
                    msgMap.get(fieldName).msgParams.add(entityClzFullName);
                } else {
                    callbackInterface.appendMethod("void " + fieldName + "();\n");
                }
            }
        }

        try { // write the file
            JavaFileObject source = mFiler.createSourceFile(pkgName +"."+ newClzName);
            Writer writer = source.openWriter();
            writer.write(callbackInterface.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // Note: calling e.printStackTrace() will print IO errors
            // that occur from the file already existing after its first run, this is normal
        }

    }

    private void mkMsgTargetBroker(Element classElement,HashMap<String ,BuildClass> clzMap) {

        if(classElement.getKind() != ElementKind.METHOD){
            return;
        }
        if(classElement.getModifiers().contains(Modifier.PRIVATE))
            return;
        if(classElement.getModifiers().contains(Modifier.PROTECTED))
            return;

        CallbackMethod cbm = classElement.getAnnotation(CallbackMethod.class);


        String clzSimpleName = classElement.getEnclosingElement().getSimpleName().toString();
        String msgId = cbm.value();
        String methodName = classElement.getSimpleName().toString();
        String pkgName = elementUtils.getPackageOf(classElement).toString();
        ExecutableElement element =  (ExecutableElement)classElement;

        if(msgId==null||msgId.equals(""))
            msgId = methodName;

        String  mtbSwitch = "";
        if(!mtbSwitchMap.containsKey(clzSimpleName+"_CbBroker")) {
            mtbSwitchMap.put(clzSimpleName+"_CbBroker", "switch(msgId){\n");
        }

        if(!msgIdsArray.containsKey(clzSimpleName+"_CbBroker")){
            msgIdsArray.put(clzSimpleName+"_CbBroker",new ArrayList<>());
        }

        msgIdsArray.get(clzSimpleName+"_CbBroker").add(msgId);

        mtbSwitch = mtbSwitchMap.get(clzSimpleName+"_CbBroker");
        mtbSwitch = mtbSwitch+"case \""+msgId+"\":\n";


        List<? extends VariableElement> parameters =  element.getParameters();

        BuildClass buildClass = null;
        if(!clzMap.containsKey(clzSimpleName+"_CbBroker")) {
            buildClass = new BuildClass();
            buildClass.setPackage(pkgName);
            buildClass.setClzModifiers("public class ");
            buildClass.setName(clzSimpleName + "_CbBroker");
            buildClass.setClzSuffix(" extends MsgTargetBroker");
            buildClass.appendField("private "+clzSimpleName+" obj;\n");

            clzMap.put(clzSimpleName+"_CbBroker",buildClass);

            buildClass.appendMethod("public void unbind(){\n");
            buildClass.appendMethod(clzSimpleName+" temp = obj;\n");
            buildClass.appendMethod("temp = null;\n");
            buildClass.appendMethod("}\n");
        }else{
            buildClass = clzMap.get(clzSimpleName+"_CbBroker");
        }

        String params = "";
        MsgIdHandler handler = msgMap.get(msgId);
        if(handler!=null){
            for(int i = 0;i<handler.msgParams.size();i++){
                if(i!=0)
                    params = params+",";
                params = params+handler.msgParams.get(i)+" o"+i;
            }
        }

        buildClass.appendMethod("public boolean " + msgId + "("+params+"){\n");

        String targetMethodParams = "";
        if(parameters!=null&&!parameters.isEmpty()) {
            for(int i=0;i<parameters.size();i++){
                if(i!=0)
                    targetMethodParams = targetMethodParams+",";
//                targetMethodParams = targetMethodParams+parameters.get(i).asType().toString()+" o"+i;
                targetMethodParams = targetMethodParams+"o"+i;
            }
        }

        buildClass.appendMethod("obj." + methodName + "("+targetMethodParams+");\n");
        buildClass.appendMethod("return "+cbm.intercept()+";\n");
        buildClass.appendMethod("}\n");

        if(params!=null&&!params.isEmpty())
            mtbSwitch = mtbSwitch+"intercept = "+msgId+"(("+handler.msgParams.get(0)+")body);\n";
        else
            mtbSwitch = mtbSwitch+msgId+"();\n";

        mtbSwitch = mtbSwitch+"break;\n";

        mtbSwitchMap.put(clzSimpleName+"_CbBroker", mtbSwitch);

//        buildClass.appendMethod("public "+clzSimpleName+"_CbBroker(){\n");
    }

    private void print(String msg){
        System.out.println("MessageParserProcessor:"+msg);
    }

    private class MsgIdHandler{
        public String msgId;
        public ArrayList<String> msgParams = new ArrayList<>();
        public String entityClz;

        public MsgIdHandler(String msgId){
            this.msgId = msgId;
        }

        public boolean hasParams(){
            return !msgParams.isEmpty();
        }
    }
}
