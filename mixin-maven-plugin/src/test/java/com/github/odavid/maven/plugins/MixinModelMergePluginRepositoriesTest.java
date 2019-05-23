package com.github.odavid.maven.plugins;

import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MixinModelMergePluginRepositoriesTest {

    private MixinModelMerger merger = new MixinModelMerger();

    private Model mixinSourceModel = new Model();
    private Repository pluginRepository1, pluginRepository2;

    @Before
    public void setUp() {
        pluginRepository1 = new Repository();
        pluginRepository1.setId("repo-1");
        pluginRepository1.setUrl("some.non.existent.url");

        pluginRepository2 = new Repository();
        pluginRepository2.setId("repo-2");
        pluginRepository2.setUrl("some.non.existent.url.2");
    }

    @Test
    public void testEmptyTarget() {
        mixinSourceModel.addPluginRepository(pluginRepository1);
        assertEquals(1, mixinSourceModel.getPluginRepositories().size());
        assertNotNull(mixinSourceModel.getPluginRepositories().get(0));

        Model target = new Model();

        merger.mergePluginRepositories(target, mixinSourceModel);

        assertEquals(target.getPluginRepositories(), mixinSourceModel.getPluginRepositories());
    }

    @Test
    public void testEmptySrc() {
        assertTrue(mixinSourceModel.getPluginRepositories().isEmpty());

        Model target = new Model();
        target.addPluginRepository(pluginRepository1);

        merger.mergePluginRepositories(target, mixinSourceModel);

        assertEquals(1, target.getPluginRepositories().size());
        assertTrue(target.getPluginRepositories().contains(pluginRepository1));
    }

    @Test
    public void testNoEffectMerge() {
        mixinSourceModel.addPluginRepository(pluginRepository1);
        assertEquals(1, mixinSourceModel.getPluginRepositories().size());
        assertNotNull(mixinSourceModel.getPluginRepositories().get(0));

        Model target = new Model();
        Repository repository = new Repository();
        repository.setId("repo-1");
        repository.setUrl("some.non.existent.url");
        target.addPluginRepository(repository);

        merger.mergePluginRepositories(target, mixinSourceModel);

        assertEquals(1, target.getPluginRepositories().size());
        assertTrue(target.getPluginRepositories().contains(pluginRepository1));
    }

    @Test
    public void testActualMerge() {
        mixinSourceModel.addPluginRepository(pluginRepository1);
        assertEquals(1, mixinSourceModel.getPluginRepositories().size());
        assertNotNull(mixinSourceModel.getPluginRepositories().get(0));

        Model target = new Model();
        target.addPluginRepository(pluginRepository2);

        merger.mergePluginRepositories(target, mixinSourceModel);

        assertEquals(2, target.getPluginRepositories().size());
        assertTrue(target.getPluginRepositories().contains(pluginRepository1));
        assertTrue(target.getPluginRepositories().contains(pluginRepository2));
    }

}
