package com.github.resource4j.objects.parsers;

import com.github.resource4j.ResourceObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class IconParser extends AbstractValueParser<Icon> {

    private static final IconParser INSTANCE = new IconParser();

    public static IconParser getInstance() {
        return INSTANCE;
    }

    @Override
    public Icon parse(ResourceObject object) throws IOException, ResourceObjectFormatException {
        BufferedImage image = ImageIO.read(object.asStream());
        if (image == null) {
        	throw new ResourceObjectFormatException(object, "Unknown image format in file {0}");
        }
        return new ImageIcon(image);
    }

}
