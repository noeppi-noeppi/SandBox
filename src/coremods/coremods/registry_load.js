"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("coremods");
function initializeCoreMod() {
    return {
        'load_registries': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.server.WorldLoader',
                'methodName': 'm_214362_',
                'methodDesc': '(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$WorldDataSupplier;Lnet/minecraft/server/WorldLoader$ResultFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;'
            },
            'transformer': function (method) {
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.DUP));
                target.add(new coremods_1.MethodInsnNode(coremods_1.Opcodes.INVOKESTATIC, 'io/github/noeppi_noeppi/mods/sandbox/impl/load/RegistryPatcher', 'patchRegistries', '(Lcom/mojang/datafixers/util/Pair;)V'));
                for (var i = 0; i < method.instructions.size(); i++) {
                    var node = method.instructions.get(i);
                    if (node.getOpcode() == coremods_1.Opcodes.INVOKEINTERFACE) {
                        var methodNode = node;
                        if (methodNode.owner == 'net/minecraft/server/WorldLoader$WorldDataSupplier' && methodNode.name == coremods_1.ASMAPI.mapMethod('m_214412_') && methodNode.desc == '(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/world/level/DataPackConfig;)Lcom/mojang/datafixers/util/Pair;') {
                            method.instructions.insert(node, target);
                            return method;
                        }
                    }
                }
                throw new Error('Failed to patch WorldLoader.class');
            }
        }
    };
}
