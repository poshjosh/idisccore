package com.idisc.core;

import com.idisc.pu.entities.Feed;
import com.bc.jpa.EntityController;
import com.idisc.core.util.StringUtil;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.htmlparser.util.Translate;

/**
 * @(#)Main.java   03-Nov-2014 08:34:36
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
public class Main {
    
    public static void main(String [] args) {
        //    \xF0\x9F\x98\x95\xF0\x9F
        String tgt = "\\xF0\\x9F\\x8C\\xBB";
        tgt = Character.toString('\u00a3');
        
        try{
System.out.println("Input: "+tgt+", encode: "+Translate.encode(tgt)+", unescapeHtml3: "+StringUtil.unescapeHtml3(tgt)+", escape: "+Util.escape(tgt));         
System.out.println("Input: "+tgt+", decode: "+Translate.decode(tgt)+", unescapeHtml3: "+StringUtil.unescapeHtml3(tgt)+", escape: "+Util.escape(tgt));         
if(true) {
    return;
}            
           System.out.println( 0 % 10); 
if(true) {
    return;
}            
            
            String str = "/news/.+?/\\d{4,}";
            Pattern pn = Pattern.compile(str);
            boolean found = pn.matcher("http://www.dailytrust.com.ng/news/politics/2019-inec-to-deploy-new-tech-for-collation/130017.html").find();
System.out.println("Found: "+found);
if(true) {
    return;
}
            
            String regex = "/201[6-9]/\\d{1,2}/";
            Pattern p = Pattern.compile(regex); 
System.out.println(p.matcher("/2016/01/").find());            
System.out.println(p.matcher("/2017/01/").find());            
System.out.println(p.matcher("/2018/01/").find());            
System.out.println(p.matcher("/2019/01/").find());            
System.out.println(p.matcher("/2020/01/").find());            
System.out.println(p.matcher("/2019/1/").find());            

if(true) {
    return;
}
            
//            Configuration config = IdiscApp.getInstance().getConfiguration();
            
//            FeedUpdateService svc = new FeedUpdateService();
            
//            svc.start(5, 900, TimeUnit.SECONDS);

            Main main = new Main();
            
            long after = System.currentTimeMillis() - 360 * 60000;
            
            List<Feed> feeds = main.getFeeds(after);
            
System.out.println("Found: "+(feeds==null?null:feeds.size()));    
            
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private String x(int outputSize) {
        String target = "0123456789";
        int start = 2;
        int end = 5;
        int gpsize = end - start;
        int rem = outputSize - gpsize;
        if(rem > 2) {
            int eachside = rem / 2;
            int sp = start - eachside;
            int addToEnd = 0;
            int addToStart;
            if(sp < 0) {
                addToEnd = 0 - sp;
                sp = 0;
            }
            int ep = end + eachside;
            if(ep >= target.length()) {
                addToStart = ep - target.length();
                ep = target.length();
                sp += addToStart;
            }else{
                ep += addToEnd;
            }
            return target.substring(sp, ep);
        }else{
            return target.substring(start, end);
        }
    }
    
    private List<Feed> getFeeds(long after) {
        
        Date startTime = new Date(after);
        
        EntityController<Feed, Object> ec = IdiscApp.getInstance().getControllerFactory().getEntityController(Feed.class);

        EntityManager em = ec.getEntityManager();
        try{
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Feed> query = cb.createQuery(Feed.class);
            Root<Feed> feed = query.from(Feed.class);
//@column literal not good            
            query.where(cb.greaterThan(feed.<Date>get("feeddate"), startTime));
            query.orderBy(cb.desc(feed.get("feeddate")));
            TypedQuery<Feed> typedQuery = em.createQuery(query);
            typedQuery.setFirstResult(0);
            typedQuery.setMaxResults(17);
            return typedQuery.getResultList();
        }finally{
            em.close();
        }
    }
    
    public Feed getFeed() {
        
        Feed feed = new Feed();
        
        feed.setAuthor("Chinomso Ikwuagwu");
        feed.setCategories("self generated");
        feed.setContent("Jesus is Lord of all! The same yesterday and today and forever.");
        feed.setDatecreated(new Date());
        feed.setDescription("Talks of Jesus' forever lordship");
        feed.setFeeddate(new Date());
        feed.setKeywords("Jesus, Lord, same forever");
        feed.setRawid("BC0000001");
        
        return feed;
    }   
}
