package com.github.odavid.maven.plugins;

import org.apache.maven.model.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by eyal on 31/05/2015.
 */
public class MixinModelMergeScmTest {
    private MixinModelMerger merger;
    private Model scm1, emptyScm;

    @Before
    public void setup(){
        merger = new MixinModelMerger();
        scm1 = new Model();
        scm1.setScm( new Scm() );
        scm1.getScm().setUrl("some.non.existent.url");

        emptyScm = new Model();
    }

    @Test
    public void testEmptyTarget(){
        Scm origScm = scm1.getScm();
        assertNotNull(origScm);
        merger.mergeScm(scm1, emptyScm);
        assertEquals(origScm, scm1.getScm());
    }
    @Test
    public void testEmptySrc(){
        assertNull(emptyScm.getScm());
        merger.mergeScm(emptyScm, scm1);
        assertNotNull(emptyScm.getScm());
        assertEquals(emptyScm.getScm().getUrl(), scm1.getScm().getUrl());
        assertNull(emptyScm.getScm().getDeveloperConnection());
    }
    @Test
    public void testNoEffectMerge(){
        Model scm2 = new Model();
        scm2.setScm(new Scm());
        scm2.getScm().setConnection( "some.stupid.url" );
        scm2.getScm().setDeveloperConnection( "some.other.stupid.url" );
        merger.mergeScm(scm2,scm1);
        assertNotNull(scm2.getScm());
        assertEquals(scm2.getScm().getConnection(), "some.stupid.url" );
        assertEquals(scm2.getScm().getDeveloperConnection(), "some.other.stupid.url" );
    }
    @Test
    public void testActualMerge(){
        Model scm2 = new Model();
        scm2.setScm(new Scm());
        scm2.getScm().setDeveloperConnection( "some.other.stupid.url" );
        merger.mergeScm(scm2,scm1);
        assertNotNull(scm2.getScm());
        assertEquals(scm2.getScm().getConnection(), scm1.getScm().getConnection() );
        assertEquals(scm2.getScm().getDeveloperConnection(), "some.other.stupid.url" );
    }
}
