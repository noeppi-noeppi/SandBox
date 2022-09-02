"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var coremods_1 = require("../coremods");
function initializeCoreMod() {
    return {
        'make_holders_valid': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.core.Holder$Reference',
                'methodName': 'm_203401_',
                'methodDesc': '(Lnet/minecraft/core/Registry;)Z'
            },
            'transformer': function (method) {
                var label = new coremods_1.LabelNode();
                var target = new coremods_1.InsnList();
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 1));
                target.add(new coremods_1.TypeInsnNode(coremods_1.Opcodes.INSTANCEOF, 'io/github/noeppi_noeppi/mods/sandbox/impl/WorldGenRegistry'));
                target.add(new coremods_1.JumpInsnNode(coremods_1.Opcodes.IFEQ, label));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 0));
                target.add(new coremods_1.VarInsnNode(coremods_1.Opcodes.ALOAD, 1));
                target.add(new coremods_1.MethodInsnNode(coremods_1.Opcodes.INVOKESTATIC, 'io/github/noeppi_noeppi/mods/sandbox/impl/WorldGenRegistry', 'forceValid', '(Lnet/minecraft/core/Holder$Reference;Lnet/minecraft/core/Registry;)Z'));
                target.add(new coremods_1.JumpInsnNode(coremods_1.Opcodes.IFEQ, label));
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.ICONST_1));
                target.add(new coremods_1.InsnNode(coremods_1.Opcodes.IRETURN));
                target.add(label);
                method.instructions.insert(target);
                return method;
            }
        }
    };
}
