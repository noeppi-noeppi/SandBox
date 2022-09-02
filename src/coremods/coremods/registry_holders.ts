import {
  CoreMods,
  InsnList,
  InsnNode,
  JumpInsnNode,
  LabelNode, MethodInsnNode,
  MethodNode,
  Opcodes,
  TypeInsnNode,
  VarInsnNode
} from "../coremods";

function initializeCoreMod(): CoreMods {
  return {
    'make_holders_valid': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.core.Holder$Reference',
        'methodName': 'm_203401_',
        'methodDesc': '(Lnet/minecraft/core/Registry;)Z'
      },
      'transformer': function(method: MethodNode) {
        const label = new LabelNode();
        const target = new InsnList();
        target.add(new VarInsnNode(Opcodes.ALOAD, 1));
        target.add(new TypeInsnNode(Opcodes.INSTANCEOF, 'io/github/noeppi_noeppi/mods/sandbox/impl/WorldGenRegistry'));
        target.add(new JumpInsnNode(Opcodes.IFEQ, label));
        target.add(new VarInsnNode(Opcodes.ALOAD, 0));
        target.add(new VarInsnNode(Opcodes.ALOAD, 1));
        target.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'io/github/noeppi_noeppi/mods/sandbox/impl/WorldGenRegistry', 'forceValid', '(Lnet/minecraft/core/Holder$Reference;Lnet/minecraft/core/Registry;)Z'));
        target.add(new JumpInsnNode(Opcodes.IFEQ, label));
        target.add(new InsnNode(Opcodes.ICONST_1));
        target.add(new InsnNode(Opcodes.IRETURN));
        target.add(label);
        
        method.instructions.insert(target);
        
        return method;
      }
    }
  }
}
