package com.idisc.core;

import com.bc.json.config.JsonConfig;
import com.bc.nodelocator.ConfigName;
import org.htmlparser.dom.HtmlDocument;
import com.bc.nodelocator.htmlparser.NodeMatcherHtmlparser;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.Test;
import com.bc.nodelocator.NodeMatcher;
import com.bc.nodelocator.NodeLocatingFilter;
import com.bc.nodelocator.impl.NodeLocatingFilterGreedy;
import com.bc.nodelocator.impl.NodeLocatingFilterImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;
import org.htmlparser.Node;
import org.htmlparser.util.NodeTreeWalker;
import org.htmlparser.visitors.NodeVisitorImpl;
import com.bc.nodelocator.Path;
import java.util.Arrays;


/**
 * @(#)TestTransverseFilter.java   09-Jun-2015 21:22:11
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class TagLocatorTest extends ExtractionTestBase {
    
    private static class NodeLocator extends NodeVisitorImpl {
        
        private final Predicate test;

        private final List<Tag> targetList = new ArrayList();

        public NodeLocator(Predicate test) {
            this.test = Objects.requireNonNull(test);
        }
        
        @Override
        public void visitTag(Tag tag) {
            if(test.test(tag)) {
                targetList.add(tag);
            }
        }
        
        public List<Tag> getTargetList() {
            return targetList == null || targetList.isEmpty() ? 
                    Collections.EMPTY_LIST : Collections.unmodifiableList(targetList);
        }
    }

    private final Class cls = null;
        
    private final boolean logVisits = false;

    private final NodeMatcher<Node> nodeMatcher;
    
    public TagLocatorTest() { 
        this.nodeMatcher = new NodeMatcherHtmlparser();
    }

    @Test
    public void test() throws Exception {
        
        if(true) {
            this.test(SiteNames.THISDAY, "https://www.thisdaylive.com/index.php/2018/08/06/agueros-brace-wins-community-shield-for-city/");
            return;
        }
        
//        final String site = this.LINDAIKEJI; // All
//        final String site = this.BELLANAIJA; // targetNode5
//        final String site = this.PUNCH_NG; // None
//        final String site = this.DAILY_TRUST; // All
//        final String site = this.NAIJ; // None
        
        final String [] names = this.getSitenames();
        
        for(String name : names) {
            this.test(name);
        }
    }
    
    public void test(String site) throws Exception {
        
        final String url = this.getUrl(site, null);
// All         final String url = "https://www.lindaikejisblog.com/2017/9/uk-government-demands-clarification-on-ipob-leader-nnamdi-kanus-where-about-from-fg.html";        
// node5      final String url = "https://www.bellanaija.com/2016/06/get-inspired-with-forbes-list-of-amercias-richest-self-made-women-oprah-winfrey-beyonce-taylor-swift-sheryl-sandberg-more/";

    }
    
    public void test(String site, String url) throws Exception {
        
        if(url == null) {
            this.log(cls, "No url for site: " + site);
            return;
        }
        
        HtmlDocument nodes = this.getNodes(url);
        
        final JsonConfig json = this.getExtractionContextFactory().getConfigService().getConfig(site, null);
        
        final int limit = json.getList(ConfigName.selectorConfigList).size();
        
        for(int i=0; i<limit; i++) {
            
            this.testTagLocator(site, i, nodes);
        }
//        this.testTagLocator(site, 5, nodes);
    }
    
    public Node testTagLocator(String site, Object key) throws IOException, ParserException {
        
        final String url = this.getUrl(site, null);
        
        HtmlDocument nodes = this.getNodes(url);

        return this.testTagLocator(site, key, nodes);
    }
    
    public Node testTagLocator(String site, Object key, HtmlDocument nodes) throws ParserException {
System.out.println();        
log(cls, "Site: " + site + ", key: " + key + ", columns: " + 
        Arrays.toString(this.getNodeExtractorConfig(site).getColumns(key)));        
        
        final Path<String> transverse = this.getNodeExtractorConfig(site).getPath(key);
//this.log(this.getClass(), "Path: " + transverse);        
        final List<String> pathFlattened = this.getNodeExtractorConfig(site).getPathFlattened(key).toList();
        
        final NodeLocator nodeLocator = this.getNodeLocator(key, pathFlattened, "", false);
        
        nodes.visitAllNodesWith(nodeLocator);

        final List<Tag> targetList = nodeLocator.getTargetList();
        Node foundTarget = targetList.isEmpty() ? null : targetList.get(0);
log(cls, nodeLocator.getClass().getSimpleName() + " Target: "+this.toString(foundTarget));

        this.process(nodes, key, pathFlattened, false);
        
        this.process(nodes, key, pathFlattened, true);
        
        final boolean depthFirst = false;
        final NodeTreeWalker walker = new NodeTreeWalker(this.getHtmlNode(nodes), depthFirst, Integer.MAX_VALUE);
        foundTarget = this.walk(NodeTreeWalker.class, walker, pathFlattened);
log(cls, "NodeTreeWalker Target: "+this.toString(foundTarget));
        
        return foundTarget;
    }
    
    public Node process(HtmlDocument nodes, Object id, List<String> path, boolean greedy) throws ParserException {
        final NodeLocator nodeLocator = this.getNodeLocator(id, path, "", greedy);
        nodes.visitAllNodesWith(nodeLocator);
        final List<Tag> targetList = nodeLocator.getTargetList();
        Node foundTarget = targetList.isEmpty() ? null : targetList.get(0);
log(cls, (greedy?"GREEDY-":"RELUCT-") + nodeLocator.getClass().getSimpleName() + " Target: "+this.toString(foundTarget));
        return foundTarget;
    }

    public Node walk(Class type, NodeIterator iter, List<String> path) throws ParserException {
        Node target = null;
        final Iterator<String> pathIter = path.iterator();
        while(pathIter.hasNext()) {
            final String pathElement = pathIter.next();
            while(iter.hasNext()) {
                final Node node = iter.next();
if(logVisits) log(cls, type.getSimpleName()+" visiting: "+this.toString(node));
                if(nodeMatcher.matches(node, pathElement)) {
                    if(!pathIter.hasNext()) {
                        target = node;
                    }
                    break;
                }
            }
        }
        return target;
    }
    
    public Node walk(Class type, NodeIterator iter, Tag targetNode) throws ParserException {
        Node foundTarget = null;
        while(iter.hasNext()) {
            final Node node = iter.next();
if(logVisits) log(cls, type.getSimpleName()+" visiting: "+this.toString(node));
            if(this.matches(targetNode, node)) {
                foundTarget = node;
                break;
            }
        }
        return foundTarget;
    }
    
    public boolean matches(Tag lhs, Node rhs) {
        boolean matches = false;
        if(lhs.equals(rhs)) {
//log(cls, "Target: "+toString(lhs)+"\nLocated by equals: "+toString(rhs));
            matches = true;
        }else{
            if(rhs instanceof Tag) {
                final Tag tag = (Tag)rhs;
                if(lhs.toTagHtml().equals(tag.toTagHtml())) {
//log(cls, "Target: "+toString(lhs)+"\nLocated by tagHtml: "+toString(rhs));
                    matches = true;
                }
            }    
        }
        return matches;
    }
    
    public Lexer getLexer(String html) {
        return new Lexer(new Page(html, StandardCharsets.UTF_8.name()));
    }
    
    public org.htmlparser.tags.Html getHtmlNode(HtmlDocument nodeList) {
        org.htmlparser.tags.Html output = null;
        for(Node node : nodeList) {
            if(node instanceof org.htmlparser.tags.Html) {
                output = (org.htmlparser.tags.Html)node;
            }
        }
        return output;
    }

    public Node getFirstParent(NodeList nodeList) {
        Node output = null;
        for(Node node : nodeList) {
            final NodeList children = node.getChildren();
            if(children != null && !children.isEmpty()) {
                output = node;
                break;
            }
        }
log(cls, "First parent: " + this.toString(output));
        return output;
    }
    
    public Node getTopmostParent(Node node) {
        Node output = node;
        while(true) {
            final Node parentNode = output.getParent();
            if(parentNode == null) {
                break;
            }
            output = output.getParent();
        }
log(cls, "Node: " + this.toString(node) + "\nTopmost parent: " + this.toString(output));
        return output;
    }

    public NodeLocator getNodeLocator(Object id, List<String> path, String prefix, boolean greedy) throws ParserException {
        final String key;
        final NodeLocatingFilter test;
        if(greedy) {
            key = "GREEDY-";
            test = new NodeLocatingFilterGreedy(id, path, nodeMatcher){
                @Override
                public String toString(Object node) {
                    return ((Tag)node).toTagHtml();
                }
            };
        }else{
            key = "RELUCT-";
            test = new NodeLocatingFilterImpl(id, path, nodeMatcher){
                @Override
                public String toString(Object node) {
                    return ((Tag)node).toTagHtml();
                }
            };
        }       
        final NodeLocator nodeVisitor = new NodeLocator(test);
        return nodeVisitor;
    }
    
    public String toString(Node node) {
        if(node instanceof Tag) {
            return ((Tag)node).toTagHtml();
        }else if (node instanceof TextNode) {
            return ((TextNode)node).getText();
        }else if(node instanceof RemarkNode) {
            return ((RemarkNode)node).getText();
        }else{
            return String.valueOf(node);
        }
    }
}
