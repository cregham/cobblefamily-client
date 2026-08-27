import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class PatchFamilyControlsSelector {
    private static final String CLIENT = "uk/co/craig/cobbleversefamily/CobbleverseFamilyControlsClient";

    public static void main(String[] args) throws IOException {
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        ClassReader reader = new ClassReader(Files.readAllBytes(input));
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        reader.accept(new org.objectweb.asm.ClassVisitor(Opcodes.ASM9, writer) {
            private boolean noOpInitializerPresent;

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if ("lambda$onInitializeClient$1".equals(name)) {
                    noOpInitializerPresent = true;
                    MethodVisitor method = super.visitMethod(access, name, descriptor, signature, exceptions);
                    method.visitCode();
                    method.visitInsn(Opcodes.RETURN);
                    method.visitMaxs(0, 2);
                    method.visitEnd();
                    return null;
                }
                if ("initializeControllerSelection".equals(name)
                        || "applyControllerSelection".equals(name)
                        || "findController".equals(name)
                        || "syncCobblemonController".equals(name)
                        || "controllerIdentity".equals(name)
                        || "findJoystickId".equals(name)
                        || "readGlfwJoystickId".equals(name)
                        || "connectedControllerIds".equals(name)
                        || "controllerName".equals(name)
                        || "applyController".equals(name)
                        || "lambda$initializeControllerSelection$7".equals(name)
                        || name.startsWith("lambda$findController$")
                        || name.startsWith("lambda$controllerName$")
                        || name.startsWith("lambda$connectedControllerIds$")) {
                    return null;
                }

                MethodVisitor next = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!"lambda$onInitializeClient$2".equals(name)) {
                    return next;
                }
                return new MethodVisitor(Opcodes.ASM9, next) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String methodName, String methodDescriptor, boolean isInterface) {
                        if (CLIENT.equals(owner) && "syncCobblemonController".equals(methodName)) {
                            return;
                        }
                        super.visitMethodInsn(opcode, owner, methodName, methodDescriptor, isInterface);
                    }
                };
            }

            @Override
            public void visitEnd() {
                if (!noOpInitializerPresent) {
                    MethodVisitor method = super.visitMethod(
                            Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC,
                            "lambda$onInitializeClient$1",
                            "(Lnet/minecraft/class_310;)V",
                            null,
                            null);
                    method.visitCode();
                    method.visitInsn(Opcodes.RETURN);
                    method.visitMaxs(0, 2);
                    method.visitEnd();
                }
                super.visitEnd();
            }
        }, 0);

        Files.write(output, writer.toByteArray());
    }
}
