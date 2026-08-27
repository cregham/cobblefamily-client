import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class PatchFlyingControls {
    public static void main(String[] args) throws IOException {
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        ClassReader reader = new ClassReader(Files.readAllBytes(input));
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        reader.accept(new org.objectweb.asm.ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor next = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (!"injectControllerInput".equals(name)) {
                    return next;
                }
                return new MethodVisitor(Opcodes.ASM9, next) {
                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof Float number && number.floatValue() == 0.15f) {
                            super.visitLdcInsn(0.25f);
                        } else if (value instanceof Float number && number.floatValue() == 1.8f) {
                            super.visitLdcInsn(1.4f);
                        } else if (value instanceof Double number && number.doubleValue() == 8.0d) {
                            super.visitLdcInsn(3.0d);
                        } else if (value instanceof Double number && number.doubleValue() == 6.0d) {
                            super.visitLdcInsn(2.5d);
                        } else {
                            super.visitLdcInsn(value);
                        }
                    }
                };
            }
        }, 0);

        Files.write(output, writer.toByteArray());
    }
}
