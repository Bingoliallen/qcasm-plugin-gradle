package com.qcplay.qcsdk

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class CustomClassVisitor extends ClassVisitor {
    private String[] classPrefixes
    private String oldSuperName
    private String newSuperName

    CustomClassVisitor(ClassVisitor classVisitor, String[] classPrefixes) {
        super(Opcodes.ASM7, classVisitor)
        this.classPrefixes = classPrefixes
    }

    @Override
    void visit(int version, int access, String className, String signature, String superName, String[] interfaces) {
        classPrefixes.each { prefix ->
                def prefixPath = prefix.replace(".", "/")
            if (className.startsWith(prefixPath)) {
                if (superName == "android/app/Activity") {
                    oldSuperName = superName
                    superName = "com/qcplay/api/plugin/base/RealBaseActivity"
                    println "${className}: ${superName}"
                    newSuperName = superName
                } else if (superName == "android/app/ActivityGroup") {
                    oldSuperName = superName
                    superName = "com/qcplay/api/plugin/base/RealBaseActivityGroup"
                    println "${className}: ${superName}"
                    newSuperName = superName
                } else if (superName == "androidx/appcompat/app/AppCompatActivity" ||
                        superName == "android/support/v7/app/AppCompatActivity") {
                    oldSuperName = superName
                    superName = "com/qcplay/api/plugin/base/RealBaseAppCompatActivity"
                    println "${className}: ${superName}"
                    newSuperName = superName
                } else if (superName == "androidx/activity/ComponentActivity" ||
                        superName == "android/support/v4/app/SupportActivity") {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseComponentActivity";
                    println "${className}: ${superName}"
                    newSuperName = superName;
                } else if (superName=="androidx/core/app/ComponentActivity") {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseCoreComponentActivity";
                    println "${className}: ${superName}"
                    newSuperName = superName;
                } else if (superName == "androidx/fragment/app/FragmentActivity"
                        || superName == "android/support/v4/app/FragmentActivity") {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseFragmentActivity";
                    println "${className}: ${superName}"
                    newSuperName = superName;
                } else if (superName == "android/app/IntentService") {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseIntentService";
                    println "${className}: ${superName}"
                    newSuperName = superName;
                } else if (superName == "android/app/job/JobService") {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseJobService";
                    println "${className}: ${superName}"
                    newSuperName = superName;
                } else if (superName == "android/app/Service") {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseService";
                    println "${className}: ${superName}"
                    newSuperName = superName;
                }
            }
        }

        super.visit(version, access, className, signature, superName, interfaces)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        def mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "<init>" && "()V" == descriptor) {
            if (newSuperName != null) {
                return new CustomMethodVisitor(mv)
            }
        }
        return mv
    }

    class CustomMethodVisitor extends MethodVisitor {
        CustomMethodVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM7, methodVisitor)
        }

        @Override
        void visitCode() {
            super.visitCode()
//            mv.visitVarInsn(Opcodes.ALOAD, 0);
//            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, newSuperName, "<init>", "()V", false);
        }

        @Override
        void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            // Adjust calls to the superclass constructor
            if (opcode == Opcodes.INVOKESPECIAL && owner == oldSuperName && name == "<init>") {
                owner = newSuperName // Change the owner to the new superclass
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }
    }
}
