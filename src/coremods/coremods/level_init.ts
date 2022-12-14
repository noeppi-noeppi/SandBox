import {
  AbstractInsnNode, ASMAPI,
  CoreMods,
  InsnList,
  InsnNode,
  MethodInsnNode,
  MethodNode,
  Opcodes,
  VarInsnNode
} from "coremods";

function initializeCoreMod(): CoreMods {
  return {
    'chunk_level_init': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.server.level.ServerLevel',
        'methodName': '<init>',
        'methodDesc': '(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/world/level/storage/ServerLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;Lnet/minecraft/server/level/progress/ChunkProgressListener;ZJLjava/util/List;Z)V'
      },
      'transformer': function(method: MethodNode) {
        const target = new InsnList();
        target.add(new InsnNode(Opcodes.DUP));
        target.add(new VarInsnNode(Opcodes.ALOAD, 1));
        target.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
          'io/github/noeppi_noeppi/mods/sandbox/impl/level/ChunkLevelInitializer',
          'initChunkGenerator', '(Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/server/MinecraftServer;)V'
        ));
        
        for (let i = 0; i < method.instructions.size(); i++) {
          const node = method.instructions.get(i) as AbstractInsnNode;
          if (node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
            const methodNode = node as MethodInsnNode;
            if (methodNode.owner == 'net/minecraft/world/level/dimension/LevelStem' && methodNode.name == ASMAPI.mapMethod('m_63990_') && methodNode.desc == '()Lnet/minecraft/world/level/chunk/ChunkGenerator;') {
              method.instructions.insert(node, target);
              return method;
            }
          }
        }
        
        throw new Error('Failed to patch ServerLevel.class');
      }
    }
  }
}
