/*
 * Copyright 2016 Rod MacKenzie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rodm.teamcity

import com.github.rodm.teamcity.tasks.DeployPlugin
import com.github.rodm.teamcity.tasks.UndeployPlugin
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static com.github.rodm.teamcity.GradleMatchers.hasTask
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.hasSize
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class EnvironmentsTest {

    private String defaultOptions = '-Dteamcity.development.mode=true -Dteamcity.development.shadowCopyClasses=true'

    private Project project

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()
    }

    @Test
    void defaultProperties() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        assertThat(extension.environments.getDownloadsDir(), equalTo('downloads'))
        assertThat(extension.environments.getBaseDownloadUrl(), equalTo('https://download.jetbrains.com/teamcity'))
        assertThat(extension.environments.getBaseDataDir(), equalTo('data'))
        assertThat(extension.environments.getBaseHomeDir(), equalTo('servers'))
    }

    @Test
    void alternativeDownloadsDir() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        project.teamcity {
            environments {
                downloadsDir = '/tmp/downloads'
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        assertThat(extension.environments.getDownloadsDir(), equalTo('/tmp/downloads'))
    }

    @Test
    void alternativeDownloadBaseUrl() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        project.teamcity {
            environments {
                baseDownloadUrl = 'http://local-repository'
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        assertThat(extension.environments.getBaseDownloadUrl(), equalTo('http://local-repository'))
    }

    @Test
    void alternativeBaseDataDir() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        project.teamcity {
            environments {
                baseDataDir = '/tmp/data'
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        assertThat(extension.environments.getBaseDataDir(), equalTo('/tmp/data'))
    }

    @Test
    void alternativeBaseHomeDir() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        project.teamcity {
            environments {
                baseHomeDir = '/tmp/servers'
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        assertThat(extension.environments.getBaseHomeDir(), equalTo('/tmp/servers'))
    }

    @Test
    void 'gradle properties should override default shared properties'() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        Project mockProject = mock(Project)
        configureGradleProjectProperties(mockProject)

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        extension.environments.project = mockProject

        assertThat(extension.environments.getDownloadsDir(), equalTo('/alt/downloads'))
        assertThat(extension.environments.getBaseDownloadUrl(), equalTo('http://alt-repository'))
        assertThat(extension.environments.getBaseHomeDir(), equalTo('/alt/servers'))
        assertThat(extension.environments.getBaseDataDir(), equalTo('/alt/data'))
    }

    @Test
    void 'gradle properties should override shared properties'() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        Project mockProject = mock(Project)
        configureGradleProjectProperties(mockProject)

        project.teamcity {
            environments {
                downloadsDir = '/tmp/downloads'
                baseDownloadUrl = 'http://local-repository'
                baseHomeDir = '/tmp/servers'
                baseDataDir = '/tmp/data'
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        extension.environments.project = mockProject

        assertThat(extension.environments.getDownloadsDir(), equalTo('/alt/downloads'))
        assertThat(extension.environments.getBaseDownloadUrl(), equalTo('http://alt-repository'))
        assertThat(extension.environments.getBaseHomeDir(), equalTo('/alt/servers'))
        assertThat(extension.environments.getBaseDataDir(), equalTo('/alt/data'))
    }

    private void configureGradleProjectProperties(Project mockProject) {
        when(mockProject.hasProperty('teamcity.environments.downloadsDir')).thenReturn(true)
        when(mockProject.property('teamcity.environments.downloadsDir')).thenReturn('/alt/downloads')
        when(mockProject.hasProperty('teamcity.environments.baseDownloadUrl')).thenReturn(true)
        when(mockProject.property('teamcity.environments.baseDownloadUrl')).thenReturn('http://alt-repository')
        when(mockProject.hasProperty('teamcity.environments.baseHomeDir')).thenReturn(true)
        when(mockProject.property('teamcity.environments.baseHomeDir')).thenReturn('/alt/servers')
        when(mockProject.hasProperty('teamcity.environments.baseDataDir')).thenReturn(true)
        when(mockProject.property('teamcity.environments.baseDataDir')).thenReturn('/alt/data')
    }

    @Test
    void defaultOptions() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        project.teamcity {
            environments {
                test {
                }
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)

        def environment = extension.environments.getByName('test')
        assertThat(environment.serverOptions, equalTo(defaultOptions))
        assertThat(environment.agentOptions, equalTo(''))
    }

    @Test
    void replaceDefaultServerOptions() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        project.teamcity {
            environments {
                test {
                    serverOptions = '-DnewOption=test'
                }
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)

        def environment = extension.environments.getByName('test')
        assertThat(environment.serverOptions, equalTo('-DnewOption=test'))
    }

    @Test
    void replaceDefaultServerOptionsWithMultipleValues() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        project.teamcity {
            environments {
                test {
                    serverOptions = ['-Doption1=value1', '-Doption2=value2']
                }
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)

        def environment = extension.environments.getByName('test')
        assertThat(environment.serverOptions, equalTo('-Doption1=value1 -Doption2=value2'))
    }

    @Test
    void addToDefaultServerOptions() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        project.teamcity {
            environments {
                test {
                    serverOptions '-DadditionalOption=test'
                }
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)

        def environment = extension.environments.getByName('test')
        assertThat(environment.serverOptions, equalTo(defaultOptions + ' -DadditionalOption=test'))
    }

    @Test
    void addMultipleValuesToDefaultServerOptions() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        project.teamcity {
            environments {
                test {
                    serverOptions '-DadditionalOption1=value1', '-DadditionalOption2=value2'
                }
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)

        def environment = extension.environments.getByName('test')
        assertThat(environment.serverOptions, equalTo(defaultOptions + ' -DadditionalOption1=value1 -DadditionalOption2=value2'))
    }

    @Test
    void replaceDefaultAgentOptions() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        project.teamcity {
            environments {
                test {
                    agentOptions = '-DnewOption1=value1'
                    agentOptions = '-DnewOption2=value2'
                }
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)

        def environment = extension.environments.getByName('test')
        assertThat(environment.agentOptions, equalTo('-DnewOption2=value2'))
    }
    @Test
    void replaceDefaultAdentOptionsWithMultipleValues() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        project.teamcity {
            environments {
                test {
                    agentOptions = ['-Doption1=value1', '-Doption2=value2']
                }
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)

        def environment = extension.environments.getByName('test')
        assertThat(environment.agentOptions, equalTo('-Doption1=value1 -Doption2=value2'))
    }

    @Test
    void addToDefaultAgentOptions() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        project.teamcity {
            environments {
                test {
                    agentOptions '-DadditionalOption1=value1'
                    agentOptions '-DadditionalOption2=value2'
                }
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)

        def environment = extension.environments.getByName('test')
        String expectedOptions = '-DadditionalOption1=value1 -DadditionalOption2=value2'
        assertThat(environment.agentOptions.trim(), equalTo(expectedOptions))
    }

    @Test
    void addMultipleValuesToDefaultAgentOptions() {
        project.apply plugin: 'com.github.rodm.teamcity-server'

        project.teamcity {
            environments {
                test {
                    agentOptions '-DadditionalOption1=value1', '-DadditionalOption2=value2'
                }
            }
        }

        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)

        def environment = extension.environments.getByName('test')
        assertThat(environment.agentOptions, equalTo('-DadditionalOption1=value1 -DadditionalOption2=value2'))
    }

    @Test
    void 'configure multiple plugins for an environment'() {
        project.apply plugin: 'com.github.rodm.teamcity-server'
        project.teamcity {
            environments {
                test {
                    plugins 'plugin1.zip'
                    plugins 'plugin2.zip'
                }
            }
        }
        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)

        TeamCityEnvironment environment = extension.environments.getByName('test')
        assertThat(environment.plugins, hasSize(2))
        assertThat(environment.plugins, hasItem('plugin1.zip'))
        assertThat(environment.plugins, hasItem('plugin2.zip'))
    }

    @Test
    void 'configure plugin with assignment replaces'() {
        project.apply plugin: 'com.github.rodm.teamcity-server'
        project.teamcity {
            environments {
                test {
                    plugins = 'plugin1.zip'
                    plugins = 'plugin2.zip'
                }
            }
        }
        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)

        TeamCityEnvironment environment = extension.environments.getByName('test')
        assertThat(environment.plugins, hasSize(1))
        assertThat(environment.plugins, hasItem('plugin2.zip'))
    }

    @Test
    void 'configure multiple plugins for an environment with a list'() {
        project.apply plugin: 'com.github.rodm.teamcity-server'
        project.teamcity {
            environments {
                test {
                    plugins = ['plugin1.zip', 'plugin2.zip']
                }
            }
        }
        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)

        TeamCityEnvironment environment = extension.environments.getByName('test')
        assertThat(environment.plugins, hasSize(2))
        assertThat(environment.plugins, hasItem('plugin1.zip'))
        assertThat(environment.plugins, hasItem('plugin2.zip'))
    }

    @Test
    void 'ConfigureEnvironmentTasks configures environments with default properties'() {
        project.apply plugin: 'com.github.rodm.teamcity-server'
        project.teamcity {
            environments {
                test1 {
                    version = '9.1.7'
                }
                test2 {
                    version = '10.0.4'
                }
            }
        }
        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        Action<Project> configureEnvironmentTasks = new TeamCityServerPlugin.ConfigureEnvironmentTasksAction(extension)

        configureEnvironmentTasks.execute(project)

        def environment1 = extension.environments.getByName('test1')
        assertThat(environment1.downloadUrl, equalTo('https://download.jetbrains.com/teamcity/TeamCity-9.1.7.tar.gz'))
        assertThat(normalizePath(environment1.homeDir), endsWith('projectDir/servers/TeamCity-9.1.7'))
        assertThat(normalizePath(environment1.dataDir), endsWith('projectDir/data/9.1'))

        def environment2 = extension.environments.getByName('test2')
        assertThat(environment2.downloadUrl, equalTo('https://download.jetbrains.com/teamcity/TeamCity-10.0.4.tar.gz'))
        assertThat(normalizePath(environment2.homeDir), endsWith('projectDir/servers/TeamCity-10.0.4'))
        assertThat(normalizePath(environment2.dataDir), endsWith('projectDir/data/10.0'))
    }

    @Test
    void 'ConfigureEnvironmentTasks configures environments using shared properties'() {
        project.apply plugin: 'com.github.rodm.teamcity-server'
        project.teamcity {
            environments {
                baseDownloadUrl = 'http://local-repository'
                baseHomeDir = '/tmp/servers'
                baseDataDir = '/tmp/data'
                test1 {
                    version = '9.1.7'
                }
                test2 {
                    version = '10.0.4'
                }
            }
        }
        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        Action<Project> configureEnvironmentTasks = new TeamCityServerPlugin.ConfigureEnvironmentTasksAction(extension)

        configureEnvironmentTasks.execute(project)

        def environment1 = extension.environments.getByName('test1')
        assertThat(environment1.downloadUrl, equalTo('http://local-repository/TeamCity-9.1.7.tar.gz'))
        assertThat(normalizePath(environment1.homeDir), endsWith('/tmp/servers/TeamCity-9.1.7'))
        assertThat(normalizePath(environment1.dataDir), endsWith('/tmp/data/9.1'))

        def environment2 = extension.environments.getByName('test2')
        assertThat(environment2.downloadUrl, equalTo('http://local-repository/TeamCity-10.0.4.tar.gz'))
        assertThat(normalizePath(environment2.homeDir), endsWith('/tmp/servers/TeamCity-10.0.4'))
        assertThat(normalizePath(environment2.dataDir), endsWith('/tmp/data/10.0'))
    }

    @Test
    void 'ConfigureEnvironmentTasks configures environment using environment properties'() {
        project.apply plugin: 'com.github.rodm.teamcity-server'
        project.teamcity {
            environments {
                test {
                    version = '9.1.7'
                    downloadUrl = 'http://local-repository/TeamCity-9.1.7.tar.gz'
                    homeDir = project.file('/tmp/servers/TeamCity-9.1.7')
                    dataDir = project.file('/tmp/data/teamcity9.1')
                    javaHome = project.file('/tmp/java')
                }
            }
        }
        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        Action<Project> configureEnvironmentTasks = new TeamCityServerPlugin.ConfigureEnvironmentTasksAction(extension)

        configureEnvironmentTasks.execute(project)

        def environment = extension.environments.getByName('test')
        assertThat(environment.downloadUrl, equalTo('http://local-repository/TeamCity-9.1.7.tar.gz'))
        assertThat(normalizePath(environment.homeDir), endsWith('/tmp/servers/TeamCity-9.1.7'))
        assertThat(normalizePath(environment.dataDir), endsWith('/tmp/data/teamcity9.1'))
        assertThat(normalizePath(environment.javaHome), endsWith('/tmp/java'))
    }

    @Test
    void 'ConfigureEnvironmentTasks adds tasks for an environment'() {
        project.apply plugin: 'com.github.rodm.teamcity-server'
        project.teamcity {
            environments {
                teamcity9 {
                    version = '9.1.7'
                }
            }
        }
        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        Action<Project> configureEnvironmentTasks = new TeamCityServerPlugin.ConfigureEnvironmentTasksAction(extension)

        configureEnvironmentTasks.execute(project)

        assertThat(project.tasks, hasTask('downloadTeamcity9'))
        assertThat(project.tasks, hasTask('unpackTeamcity9'))
        assertThat(project.tasks, hasTask('installTeamcity9'))

        assertThat(project.tasks, hasTask('deployPluginToTeamcity9'))
        assertThat(project.tasks, hasTask('undeployPluginFromTeamcity9'))

        assertThat(project.tasks, hasTask('startTeamcity9Server'))
        assertThat(project.tasks, hasTask('stopTeamcity9Server'))
        assertThat(project.tasks, hasTask('startTeamcity9Agent'))
        assertThat(project.tasks, hasTask('stopTeamcity9Agent'))
    }

    @Test
    void 'ConfigureEnvironmentTasks adds tasks for each separate environment'() {
        project.apply plugin: 'com.github.rodm.teamcity-server'
        project.teamcity {
            environments {
                teamcity9 {
                    version = '9.1.7'
                }
                teamcity10 {
                    version = '10.0.3'
                }
            }
        }
        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        Action<Project> configureEnvironmentTasks = new TeamCityServerPlugin.ConfigureEnvironmentTasksAction(extension)

        configureEnvironmentTasks.execute(project)

        assertThat(project.tasks, hasTask('startTeamcity9Server'))
        assertThat(project.tasks, hasTask('startTeamcity10Server'))
    }

    @Test
    void 'ConfigureEnvironmentTasks configures deploy and undeploy tasks with project plugin'() {
        project.apply plugin: 'com.github.rodm.teamcity-server'
        project.teamcity {
            environments {
                teamcity10 {
                    version = '10.0.3'
                }
            }
        }
        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        Action<Project> configureEnvironmentTasks = new TeamCityServerPlugin.ConfigureEnvironmentTasksAction(extension)

        configureEnvironmentTasks.execute(project)

        DeployPlugin deployPlugin = project.tasks.getByName('deployPluginToTeamcity10')
        assertThat(deployPlugin.files.files, hasSize(1))
        assertThat(deployPlugin.files.files, hasItem(new File(project.rootDir, 'build/distributions/test.zip')))

        UndeployPlugin undeployPlugin = project.tasks.getByName('undeployPluginFromTeamcity10')
        assertThat(undeployPlugin.files.files, hasSize(1))
        assertThat(undeployPlugin.files.files, hasItem(new File(project.rootDir, 'data/10.0/plugins/test.zip')))
    }

    @Test
    void 'ConfigureEnvironmentTasks configures deploy and undeploy tasks with environment plugins'() {
        project.apply plugin: 'com.github.rodm.teamcity-server'
        project.teamcity {
            environments {
                teamcity10 {
                    version = '10.0.3'
                    plugins 'plugin1.zip'
                    plugins 'plugin2.zip'
                }
            }
        }
        TeamCityPluginExtension extension = project.extensions.getByType(TeamCityPluginExtension)
        Action<Project> configureEnvironmentTasks = new TeamCityServerPlugin.ConfigureEnvironmentTasksAction(extension)

        configureEnvironmentTasks.execute(project)

        DeployPlugin deployPlugin = project.tasks.getByName('deployPluginToTeamcity10')
        assertThat(deployPlugin.files.files, hasSize(2))
        assertThat(deployPlugin.files.files, hasItem(new File(project.rootDir, 'plugin1.zip')))
        assertThat(deployPlugin.files.files, hasItem(new File(project.rootDir, 'plugin2.zip')))

        UndeployPlugin undeployPlugin = project.tasks.getByName('undeployPluginFromTeamcity10')
        assertThat(undeployPlugin.files.files, hasSize(2))
        assertThat(undeployPlugin.files.files, hasItem(new File(project.rootDir, 'data/10.0/plugins/plugin1.zip')))
        assertThat(undeployPlugin.files.files, hasItem(new File(project.rootDir, 'data/10.0/plugins/plugin2.zip')))
    }

    private String normalizePath(File path) {
        path.canonicalPath.replace('\\', '/')
    }
}
