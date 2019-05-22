package com.github.odavid.maven.plugins;

import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MixinModelMergeRepositoriesTest {

    private MixinModelMerger merger = new MixinModelMerger();

    private Model mixinSourceModel = new Model();
    private Repository repository1, repository2;

    @Before
    public void setUp() {
        repository1 = new Repository();
        repository1.setId("repo-1");
        repository1.setUrl("some.non.existent.url");

        repository2 = new Repository();
        repository2.setId("repo-2");
        repository2.setUrl("some.non.existent.url.2");
    }

    @Test
    public void testEmptyTarget() {
        mixinSourceModel.addRepository(repository1);
        assertEquals(1, mixinSourceModel.getRepositories().size());
        assertNotNull(mixinSourceModel.getRepositories().get(0));

        Model target = new Model();

        merger.mergeRepositories(target, mixinSourceModel);

        assertEquals(target.getRepositories(), mixinSourceModel.getRepositories());
    }

    @Test
    public void testEmptySrc() {
        assertTrue(mixinSourceModel.getRepositories().isEmpty());

        Model target = new Model();
        target.addRepository(repository1);

        merger.mergeRepositories(target, mixinSourceModel);

        assertEquals(1, target.getRepositories().size());
        assertTrue(target.getRepositories().contains(repository1));
    }

    @Test
    public void testNoEffectMerge() {
        mixinSourceModel.addRepository(repository1);
        assertEquals(1, mixinSourceModel.getRepositories().size());
        assertNotNull(mixinSourceModel.getRepositories().get(0));

        Model target = new Model();
        Repository repository = new Repository();
        repository.setId("repo-1");
        repository.setUrl("some.non.existent.url");
        target.addRepository(repository);

        merger.mergeRepositories(target, mixinSourceModel);

        assertEquals(1, target.getRepositories().size());
        assertTrue(target.getRepositories().contains(repository1));
    }

    @Test
    public void testActualMerge() {
        mixinSourceModel.addRepository(repository1);
        assertEquals(1, mixinSourceModel.getRepositories().size());
        assertNotNull(mixinSourceModel.getRepositories().get(0));

        Model target = new Model();
        target.addRepository(repository2);

        merger.mergeRepositories(target, mixinSourceModel);

        assertEquals(2, target.getRepositories().size());
        assertTrue(target.getRepositories().contains(repository1));
        assertTrue(target.getRepositories().contains(repository2));
    }

}
