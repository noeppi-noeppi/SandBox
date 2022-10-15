import {AbstractInsnNode, ASMAPI, CoreMods, InsnList, InsnNode, MethodInsnNode, MethodNode, Opcodes} from "coremods";

function initializeCoreMod(): CoreMods {
  return {
    'load_registries': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.server.WorldLoader',
        'methodName': 'm_214362_',
        'methodDesc': '(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;Lnet/minecraft/server/WorldLoader$ResultFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;'
      },
      'transformer': (method: MethodNode) => {
        const target = new InsnList();
        target.add(new InsnNode(Opcodes.DUP))
        target.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
          'io/github/noeppi_noeppi/mods/sandbox/impl/load/RegistryPatcher',
          'patchRegistries', '(Lcom/mojang/datafixers/util/Pair;)V'
        ))

        for (let i = 0; i < method.instructions.size(); i++) {
          const node = method.instructions.get(i) as AbstractInsnNode;
          if (node.getOpcode() == Opcodes.INVOKEINTERFACE) {
            const methodNode = node as MethodInsnNode;
            if (methodNode.owner == 'net/minecraft/server/WorldLoader$WorldDataSupplier' && methodNode.name == ASMAPI.mapMethod('m_214412_') && methodNode.desc == '(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/world/level/DataPackConfig;)Lcom/mojang/datafixers/util/Pair;') {
              method.instructions.insert(node, target);
              return method;
            }
          }
        }

        throw new Error('Failed to patch WorldLoader.class');
      }
    }
  }
}
