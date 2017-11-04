package com.idisc.core;

import com.idisc.core.comparator.feed.FeedComparatorUserSiteHitcount;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feedhit;
import com.idisc.pu.entities.Installation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.junit.BeforeClass;
import org.junit.Test;
import com.bc.jpa.context.JpaContext;

/**
 * @author poshjosh
 */
public class FeedComparatorTest {
    
    private static IdiscTestBase base;
    
    public FeedComparatorTest() { }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        base = new IdiscTestBase();
    }

    @Test
    public void test() {
        JpaContext cf = base.getIdiscApp().getJpaContext();
        EntityManager em = cf.getEntityManager(Feedhit.class);
        Installation installation = em.find(Installation.class, 2);
        List<Feed> feeds;
        try{
            final int offset = 470_000;
            final int length = 200;
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Feed> cq = cb.createQuery(Feed.class);
            Root<Feed> root = cq.from(Feed.class);
            cq.select(root);
            Predicate feedIdGreaterThan = cb.gt(root.<Integer>get("feedid"), offset);
            cq.where(feedIdGreaterThan);
            TypedQuery<Feed> query = em.createQuery(cq);
            query.setFirstResult(0);
            query.setMaxResults(length);
            feeds = query.getResultList();
        }finally{
            em.close();
        }
long tb4 = System.currentTimeMillis();
long mb4 = com.bc.util.Util.availableMemory();
        try (FeedComparatorUserSiteHitcount fc = new FeedComparatorUserSiteHitcount(installation)) {
            Collections.sort(feeds, fc);
        }
this.logTimeAndMemoryConsumed(feeds.size(), tb4, mb4);
this.printFeedids(feeds);
        try (FeedComparatorUserSiteHitcount fc = new FeedComparatorUserSiteHitcount(null)) {
            Collections.sort(feeds, fc);
        }
this.logTimeAndMemoryConsumed(feeds.size(), tb4, mb4);
this.printFeedids(feeds);
    }

    private void logTimeAndMemoryConsumed(long numberOfFeeds, long tb4, long mb4) {
System.out.println("Compared: "+(numberOfFeeds)+" feeds, consumed time: "+
(System.currentTimeMillis()-tb4)+", memory: "+(mb4-com.bc.util.Util.usedMemory(mb4)));        
    }

    private void printFeedids(Collection<Feed> feeds) {
        StringBuilder builder = new StringBuilder();
        for(Feed feed:feeds) {
            builder.append(feed.getFeedid()).append(',');
        }
System.out.println(builder);        
    }
}
