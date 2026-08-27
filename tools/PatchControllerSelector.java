import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

public final class PatchControllerSelector {
    private static final String TARGET = "com/example/client/controller/ControllerConfigScreen";

    public static void main(String[] args) throws IOException {
        Path input = Path.of(args[0]);
        Path output = Path.of(args[1]);
        ClassReader reader = new ClassReader(input.toFile().toURI().toURL().openStream());
        ClassNode node = new ClassNode(Opcodes.ASM9);
        reader.accept(node, 0);

        for (MethodNode method : node.methods) {
            if ("getControllerSelectionIndex".equals(method.name)) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() == Opcodes.ICONST_2
                            && instruction.getNext() != null
                            && instruction.getNext().getOpcode() == Opcodes.ILOAD) {
                        method.instructions.set(instruction, new InsnNode(Opcodes.ICONST_3));
                    }
                }
            }
            for (AbstractInsnNode current : method.instructions.toArray()) {
                if (!(current instanceof LdcInsnNode ldc) || !"Any Controller".equals(ldc.cst)) {
                    continue;
                }

                ldc.cst = "Controller 1";
                boolean inSelectorArray = false;
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction == current) {
                        inSelectorArray = true;
                    } else if (inSelectorArray && instruction instanceof LdcInsnNode label) {
                        if ("Controller 0".equals(label.cst)) label.cst = "Controller 2";
                        if ("Controller 1".equals(label.cst)) label.cst = "Controller 3";
                    } else if (inSelectorArray && instruction.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        break;
                    }
                }

                for (AbstractInsnNode instruction = current.getPrevious(); instruction != null; instruction = instruction.getPrevious()) {
                    if (instruction instanceof TypeInsnNode type && instruction.getOpcode() == Opcodes.ANEWARRAY
                            && "java/lang/String".equals(type.desc)) {
                        AbstractInsnNode size = instruction.getPrevious();
                        if (size != null && size.getOpcode() == Opcodes.ICONST_3) {
                            method.instructions.set(size, new InsnNode(Opcodes.ICONST_4));
                            AbstractInsnNode optionCount = size.getPrevious();
                            if (optionCount != null && optionCount.getOpcode() == Opcodes.ICONST_3) {
                                method.instructions.set(optionCount, new InsnNode(Opcodes.ICONST_4));
                            }
                            break;
                        }
                    }
                }

                method.instructions.insert(findLastArrayStore(method, current), new InsnList() {{
                    add(new InsnNode(Opcodes.DUP));
                    add(new InsnNode(Opcodes.ICONST_3));
                    add(new LdcInsnNode("Controller 4"));
                    add(new InsnNode(Opcodes.AASTORE));
                }});
                break;
            }

            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call) || !"addCycle".equals(call.name)) {
                    continue;
                }
                for (AbstractInsnNode previous = instruction.getPrevious(); previous != null; previous = previous.getPrevious()) {
                    if (previous instanceof TypeInsnNode type && previous.getOpcode() == Opcodes.ANEWARRAY
                            && "java/lang/String".equals(type.desc)) {
                        AbstractInsnNode arraySize = previous.getPrevious();
                        AbstractInsnNode optionCount = arraySize == null ? null : arraySize.getPrevious();
                        if (optionCount != null && optionCount.getOpcode() == Opcodes.ICONST_3) {
                            method.instructions.set(optionCount, new InsnNode(Opcodes.ICONST_4));
                        }
                        break;
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);

        Files.write(output, writer.toByteArray());
    }

    private static AbstractInsnNode findLastArrayStore(MethodNode method, AbstractInsnNode start) {
        AbstractInsnNode result = null;
        boolean found = false;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction == start) found = true;
            if (found && instruction.getOpcode() == Opcodes.AASTORE) result = instruction;
            if (found && instruction.getOpcode() == Opcodes.INVOKEVIRTUAL) break;
        }
        return result;
    }
}
