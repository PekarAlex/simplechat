package org.ompekar.chat;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;

public class VelocityEscapeDirective extends Directive{

    public String getName() {
        return "escape";
    }

    public int getType() {
        return LINE;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node)
            throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        //setting default params
        String processString = null;

        //reading params
        if (node.jjtGetChild(0) != null) {
            processString = String.valueOf(node.jjtGetChild(0).value(context));
        }


        //process and write result to writer
        processString= StringUtils.replaceEach(processString, new String[]{"&", "<", ">", "\"", "'", "/"}, new String[]{"&amp;", "&lt;", "&gt;", "&quot;", "&#x27;", "&#x2F;"});
        processString = processString.replaceAll("(\r\n|\n)", "<br />");

        writer.write(processString);

        return true;

    }


}






