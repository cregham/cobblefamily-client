import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public final class PatchControllerSelectionPersistence {
    public static void main(String[] args) throws IOException {
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        ClassNode node = new ClassNode(Opcodes.ASM9);
        new ClassReader(Files.readAllBytes(input)).accept(node, 0);

        for (var method : node.methods) {
            if ("getControllerSelectionIndex".equals(method.name)) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() == Opcodes.ICONST_1) {
                        method.instructions.set(instruction, new InsnNode(Opcodes.NOP));
                    }
                    if (instruction.getOpcode() == Opcodes.IADD) {
                        method.instructions.set(instruction, new InsnNode(Opcodes.NOP));
                    }
                    if (instruction.getOpcode() == Opcodes.ICONST_3) {
                        method.instructions.set(instruction, new InsnNode(Opcodes.ICONST_4));
                    }
                }
            } else if ("setControllerSelectionFromIndex".equals(method.name)) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() == Opcodes.ICONST_1) {
                        method.instructions.set(instruction, new InsnNode(Opcodes.NOP));
                    }
                    if (instruction.getOpcode() == Opcodes.ISUB) {
                        method.instructions.set(instruction, new InsnNode(Opcodes.NOP));
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        Files.write(output, writer.toByteArray());
    }
}
