package org.jbpm.process.svg.generate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.junit.Test;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SVGImageGeneratorTest {

    @Test
    public void generateSVG() throws Exception {

        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        Document doc = impl.createDocument(svgNS, "svg", null);

        // Get the root element (the 'svg' element).
        Element svgRoot = doc.getDocumentElement();

        // Set the width and height attributes on the root 'svg' element.
        svgRoot.setAttributeNS(null, "width", "100");
        svgRoot.setAttributeNS(null, "height", "100");

        // Create the rectangle.
        Element rectangle = doc.createElementNS(svgNS, "rect");
        rectangle.setAttributeNS(null, "x", "10");
        rectangle.setAttributeNS(null, "y", "20");
        rectangle.setAttributeNS(null, "width", "100");
        rectangle.setAttributeNS(null, "height", "50");
        rectangle.setAttributeNS(null, "fill", "red");

        // Attach the rectangle to the root 'svg' element.
        svgRoot.appendChild(rectangle);
        
        displaySVG(doc);
    } 
       
    
    public static void displaySVG(Document doc) throws Exception {
        File svgFile = File.createTempFile("svg", ".svg");
        FileOutputStream outputStream = new FileOutputStream(svgFile);
        
        Transcoder transcoder = new SVGTranscoder();
        OutputStreamWriter outWriter = new OutputStreamWriter(outputStream);
        TranscoderOutput output = new TranscoderOutput(outWriter);
        TranscoderInput input = new TranscoderInput(doc);
        
        transcoder.transcode(input, output);
        outputStream.flush();
        outputStream.close();
        if( outWriter != null ) { 
            outWriter.flush();
            outWriter.close();
        }

        SVGApplication.startup(svgFile);
        Thread.sleep(1*1000);
    }
}
