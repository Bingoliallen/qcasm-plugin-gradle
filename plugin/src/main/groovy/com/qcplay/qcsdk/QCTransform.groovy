package com.qcplay.qcsdk;

import com.android.build.api.transform.*;
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;
import org.objectweb.asm.*;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * 自定义的 Transform 类
 */
class QCTransform extends Transform {
    private Project mProject
    private final Logger logger;
    private String[] classPrefixes = null;

    public QCTransform(Project project) {
        this.mProject = project;
        this.logger = project.getLogger();
    }

    @Override
    public String getName() {
        return "CustomTransform";
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    // 当前Transform是否支持增量编译
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    public void transform(TransformInvocation transformInvocation) {
        // Transformation logic
        logger.lifecycle("Running CustomTransform...");
        if (classPrefixes == null) {
            classPrefixes = (String[]) mProject.getExtensions().getExtraProperties().get("QCAsmClassPrefixes");
            logger.lifecycle("CustomTransform classPrefixes: " + classPrefixes.toString());
        }

//        ClassPool classPool = ClassPool.getDefault();

        transformInvocation.inputs.each { input ->
            input.directoryInputs.each { directoryInput ->
                File dir = directoryInput.file
                processDirectory(dir)

                TransformOutputProvider outputProvider = transformInvocation.outputProvider
                if (outputProvider != null) {
                    File outputDir = outputProvider.getContentLocation(
                            directoryInput.name,
                            directoryInput.contentTypes,
                            directoryInput.scopes,
                            Format.DIRECTORY)
                    FileUtils.copyDirectory(dir, outputDir)
                }
            }

            input.jarInputs.each { jarInput ->
                File jarFile = jarInput.file
                File tempJar = processJar(jarFile)

                TransformOutputProvider outputProvider = transformInvocation.outputProvider
                if (outputProvider != null) {
                    File outputJar = outputProvider.getContentLocation(
                            jarInput.name,
                            jarInput.contentTypes,
                            jarInput.scopes,
                            Format.JAR)
                    FileUtils.copyFile(tempJar, outputJar)
                    tempJar.delete()
                }
            }
        }
    }

    private void processDirectory(File dir) throws IOException {
        if (!dir.isDirectory()) return

        FileUtils.listFiles(dir, ['class'] as String[], true).each { file ->
            String fileName = file.name
            if (fileName.endsWith('R.class') || fileName.endsWith('BuildConfig.class') || fileName.contains("R\$")) {
                return
            }
            FileInputStream fis = new FileInputStream(file)
            ClassReader classReader = new ClassReader(fis)
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            CustomClassVisitor classVisitor = new CustomClassVisitor(classWriter, classPrefixes)
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
            fis.close()

            FileOutputStream fos = new FileOutputStream(file)
            fos.write(classWriter.toByteArray())
            fos.close()
        }
    }

    private File processJar(File jarFile) throws IOException {
        File tempJar = File.createTempFile("temp", ".jar")
        JarFile jar = new JarFile(jarFile)
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(tempJar))
        Enumeration<JarEntry> entries = jar.entries()
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement()
            InputStream is = jar.getInputStream(entry)
            jos.putNextEntry(new JarEntry(entry.getName()))
            if (entry.getName().endsWith(".class")&& !entry.getName().endsWith("R.class")
                    && !entry.getName().endsWith("BuildConfig.class")) {
                ClassReader classReader = new ClassReader(is)
                ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                CustomClassVisitor classVisitor = new CustomClassVisitor(classWriter, classPrefixes)
                classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
                jos.write(classWriter.toByteArray())
            } else {
                IOUtils.copy(is, jos)
            }
            jos.closeEntry()
            is.close()
        }
        jos.close()
        jar.close()
        return tempJar
    }

    private void processDirectory(ClassPool classPool, File dir) {
        if (!dir.isDirectory()) return;

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                processDirectory(classPool, file);
            } else if (file.isFile() && file.getName().endsWith(".class")) {
                try {
                    String className = file.getPath()
                            .replace(dir.getPath() + File.separator, "")
                            .replace(File.separator, ".")
                            .replace(".class", "");

                    if (className.startsWith("com.qiyukf")) {//|| className."com.alipay.sdk.app"
                        CtClass ctClass = classPool.get(className);
                        if (ctClass.getSuperclass().getName() == "android.app.Activity") {
                            ctClass.setSuperclass(classPool.get("com.qcplay.api.plugin.base.RealBaseActivity"));
                            logger.lifecycle("Modified class: " + ctClass.getName());
                        } else if (ctClass.getSuperclass().getName() == "android.app.ActivityGroup") {
                            ctClass.setSuperclass(classPool.get("com.qcplay.api.plugin.base.RealBaseActivityGroup"));
                            logger.lifecycle("Modified class: " + ctClass.getName());
                        } else if (superClassName.equals("androidx.appcompat.app.AppCompatActivity") ||
                                superClassName.equals("android.support.v7.app.AppCompatActivity")) {
                            ctClass.setSuperclass(classPool.get("com.qcplay.api.plugin.base.RealBaseAppCompatActivity"));
                            logger.lifecycle("Modified class: " + ctClass.getName());
                        } else if (ctClass.getSuperclass().getName() == "androidx.activity.ComponentActivity" ||
                                ctClass.getSuperclass().getName() == "android.support.v4.app.SupportActivity") {
                            ctClass.setSuperclass(classPool.get("com.qcplay.api.plugin.base.RealBaseComponentActivity"));
                            logger.lifecycle("Modified class: " + ctClass.getName());
                        } else if (ctClass.getSuperclass().getName() == "androidx.fragment.app.FragmentActivity"
                                || ctClass.getSuperclass().getName() == "android.support.v4.app.FragmentActivity") {
                            ctClass.setSuperclass(classPool.get("com.qcplay.api.plugin.base.RealBaseFragmentActivity"));
                            logger.lifecycle("Modified class: " + ctClass.getName());
                        }

                        ctClass.writeFile(dir.getAbsolutePath());
                        ctClass.detach();
                    }
                } catch (Exception e) {
                    logger.error("Failed to modify class: " + file.getName(), e);
                }
            }
        }
    }

}
