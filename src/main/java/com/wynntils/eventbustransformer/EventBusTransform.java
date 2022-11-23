package com.wynntils.eventbustransformer;

import dev.architectury.transformer.transformers.base.ClassEditTransformer;
import net.minecraftforge.eventbus.EventBusEngine;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public class EventBusTransform implements ClassEditTransformer {

    private static final long serialVersionUID = -2304913653368586405L;
    private static final EventBusEngine engine = new EventBusEngine();

    @Override
    public dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.ClassNode doEdit(String name, dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.ClassNode node) {
        Type type = Type.getObjectType(node.name);
        if (engine.handlesClass(type)) {
            dev.architectury.transformer.shadowed.impl.org.objectweb.asm.ClassWriter architecturyClassWriter = new dev.architectury.transformer.shadowed.impl.org.objectweb.asm.ClassWriter(0);
            node.accept(architecturyClassWriter);
            ClassReader normalClassReader = new ClassReader(architecturyClassWriter.toByteArray());
            ClassNode normalNode = new ClassNode();
            normalClassReader.accept(normalNode, 0);

            engine.processClass(normalNode, type);

            ClassWriter normalClassWriter = new ClassWriter(0);
            normalNode.accept(normalClassWriter);
            dev.architectury.transformer.shadowed.impl.org.objectweb.asm.ClassReader architecturyClassReader = new dev.architectury.transformer.shadowed.impl.org.objectweb.asm.ClassReader(normalClassWriter.toByteArray());
            node = new dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.ClassNode();
            architecturyClassReader.accept(node, 0);
        }
        return node;
    }

}
