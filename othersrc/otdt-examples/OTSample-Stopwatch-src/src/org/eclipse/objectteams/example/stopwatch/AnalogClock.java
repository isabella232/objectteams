/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: AnalogClock.java 23501 2010-02-08 18:27:55Z stephan $
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.stopwatch;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class AnalogClock extends JPanel {
    private int seconds = 0;

    private static final int   spacing = 10;
    private static final float threePi = (float)(3.0 * Math.PI);
    // Angles for the trigonometric functions are measured in radians.
    // The following in the number of radians per sec or min.
    private static final float radPerSecMin = (float)(Math.PI / 30.0);

    private int size;       // height and width of clock face
    private int centerX;    // x coord of middle of clock
    private int centerY;    // y coord of middle of clock
    private BufferedImage clockImage;

    //==================================================== Clock constructor
    public AnalogClock() {
        this.setBackground(Color.white);
        this.setForeground(Color.black);
    }//end constructor

    //=============================================================== update
        // Replace the default update so that the plain background
        // doesn't get drawn.
    public void update(int value) {
    	seconds = value;
        this.repaint();
    }//end update

    //======================================================= paintComponent

    public void paintComponent(Graphics g) {
        super.paintComponent(g);  // paint background, borders
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        size = ((w < h) ? w : h) - 2 * spacing;
        centerX = size / 2 + spacing;
		centerY = size / 2 + spacing + 35;

        // Create the clock face background image if this is the first time
        if (clockImage == null
                || clockImage.getWidth() != w
                || clockImage.getHeight() != h) {

        	try {
				clockImage = ImageIO.read(new File("img/watch.jpg"));
			} catch (IOException e) {
				// File not found :-/
			}
            // now get a graphics context from this image
            Graphics2D gc = clockImage.createGraphics();
            gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
        }
        
        // Draw the clock face from the precomputed image
        g2.drawImage(clockImage, null, 0, 0);

        // Draw the clock hands
        drawClockHand(g);
    }//end paintComponent

    //======================================================= drawClockHand
    private void drawClockHand(Graphics g) {
        int secondRadius = size / 2 - 55;

        // second hand
        float fseconds = seconds;
        float secondAngle = threePi - (radPerSecMin * fseconds);
        drawRadius(g, centerX, centerY, secondAngle, 0, secondRadius);

    }//end drawClockHands

    // =========================================================== drawRadius
    private void drawRadius(Graphics g, int x, int y, double angle,
                    int minRadius, int maxRadius) {
        float sine   = (float)Math.sin(angle);
        float cosine = (float)Math.cos(angle);

        int dxmin = (int)(minRadius * sine);
        int dymin = (int)(minRadius * cosine);

        int dxmax = (int)(maxRadius * sine);
        int dymax = (int)(maxRadius * cosine);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setStroke(new BasicStroke(4));
        g2d.drawLine(x + dxmin, y + dymin, x + dxmax, y + dymax);
    }//end drawRadius

}
