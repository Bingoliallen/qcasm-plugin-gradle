package com.qcplay.qcsdk;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CustomClassVisitor extends ClassVisitor {
    private final String[] classPrefixes;
    private String oldSuperName;
    private String newSuperName;

    public CustomClassVisitor(ClassVisitor classVisitor, String[] classPrefixes) {
        super(Opcodes.ASM7, classVisitor);
        this.classPrefixes = classPrefixes;
    }

    @Override
    public void visit(int version, int access, String className, String signature, String superName, String[] interfaces) {
        for (String prefix : classPrefixes) {
            prefix = prefix.replace(".", "/");
            if (className.startsWith(prefix)) {
                if (superName.equals("android/app/Activity")) {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseActivity";
                    System.out.println(className +": "+ superName);
                    newSuperName = superName;
                } else if (superName.equals("android/app/ActivityGroup")) {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseActivityGroup";
                    System.out.println(className +": "+ superName);
                    newSuperName = superName;
                } else if (superName.equals("androidx/appcompat/app/AppCompatActivity") ||
                        superName.equals("android/support/v7/app/AppCompatActivity")) {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseAppCompatActivity";
                    System.out.println(className +": "+ superName);
                    newSuperName = superName;
                } else if (superName.equals("androidx/activity/ComponentActivity") ||
                        superName.equals("android/support/v4/app/SupportActivity")) {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseComponentActivity";
                    System.out.println(className +": "+ superName);
                    newSuperName = superName;
                } else if (superName.equals("androidx/core/app/ComponentActivity")) {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseCoreComponentActivity";
                    System.out.println(className +": "+ superName);
                    newSuperName = superName;
                } else if (superName.equals("androidx/fragment/app/FragmentActivity")
                        || superName.equals("android/support/v4/app/FragmentActivity")) {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseFragmentActivity";
                    System.out.println(className +": "+ superName);
                    newSuperName = superName;
                }

                else if (superName.equals("android/app/IntentService")) {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseIntentService";
                    System.out.println(className +": "+ superName);
                    newSuperName = superName;
                }
                 else if (superName.equals("android/app/job/JobService")) {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseJobService";
                    System.out.println(className +": "+ superName);
                    newSuperName = superName;
                }
                else if (superName.equals("android/app/Service")) {
                    oldSuperName = superName;
                    superName = "com/qcplay/api/plugin/base/RealBaseService";
                    System.out.println(className +": "+ superName);
                    newSuperName = superName;
                }
            }
        }

        super.visit(version, access, className, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (name.equals("<init>") && "()V".equals(descriptor)) {
            if (newSuperName != null) {
                return new CustomMethodVisitor(mv);
            }
        }
        return mv;
    }

    public class CustomMethodVisitor extends MethodVisitor {
        public CustomMethodVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM7, methodVisitor);
        }

        @Override
        public void visitCode() {
            super.visitCode();
//            mv.visitVarInsn(Opcodes.ALOAD, 0);
//            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, newSuperName, "<init>", "()V", false);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            // Adjust calls to the superclass constructor
            if (opcode == Opcodes.INVOKESPECIAL && owner.equals(oldSuperName) && name.equals("<init>")) {
                owner = newSuperName; // Change the owner to the new superclass
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }
}

