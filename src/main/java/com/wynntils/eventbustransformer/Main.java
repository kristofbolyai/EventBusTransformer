package com.wynntils.eventbustransformer;

import net.minecraftforge.eventbus.EventBusEngine;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger("EventBusTransformer");

    public static void main(String[] args) throws IOException {
        EventBusEngine engine = new EventBusEngine();
        
        File file = new File(args[0]);
        ZipFile zip = new ZipFile(file);
        File transformed = new File(args.length > 1 ? args[1] : "transformed.jar");
        ZipOutputStream output = new ZipOutputStream(new FileOutputStream(transformed));

        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry next = entries.nextElement();
            output.putNextEntry(next);
            byte[] content = IOUtils.toByteArray(zip.getInputStream(next));
            if (next.getName().endsWith(".class")) {
                Type type = Type.getObjectType(next.getName().replace(".class", ""));
                if (engine.handlesClass(type) && type.getClassName().startsWith("com.wynntils")) {
                    LOGGER.info("Transforming class " + type.getClassName());
                    ClassReader reader = new ClassReader(content);
                    ClassNode node = new ClassNode();
                    reader.accept(node, 0);

                    engine.processClass(node, type);
                    
                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                    node.accept(writer);

                    content = writer.toByteArray();
                }
            }

            IOUtils.write(content, output);
            output.closeEntry();
        }
        output.close();
        zip.close();

        if (args.length <= 1) {
            FileOutputStream write = new FileOutputStream(file);
            FileInputStream read = new FileInputStream(transformed);
            IOUtils.copy(read, write);
            write.close();
            read.close();
        }

    }

}
