package com.espressif.idf.tests.executable.cases.project;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.tests.common.resources.DefaultFileContentsReader;
import com.espressif.idf.tests.common.utility.TestAssertUtility;
import com.espressif.idf.tests.operations.ProjectTestOperations;
import com.espressif.idf.tests.operations.SWTBotTreeOperations;

@RunWith(SWTBotJunit4ClassRunner.class)
public class NewEspressifIDFProjectTest
{
	private Fixture fixture;

	@Before
	public void beforeEachTest() throws Exception
	{
		fixture = new Fixture();
	}

	@After
	public void afterEachTest()
	{
		fixture.cleanTestEnv();
	}

	@Test
	public void givenNewIDFProjectIsSelectedThenProjectIsCreatedAndAddedToProjectExplorer() throws Exception
	{
		fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		fixture.givenProjectNameIs("NewProjectTest");
		fixture.whenNewProjectIsSelected();
		fixture.thenProjectIsAddedToProjectExplorer();
	}
	
	@Test
	public void givenNewProjectIsSelectedTheProjectHasTheRequiredFiles() throws Exception
	{
		fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		fixture.givenProjectNameIs("NewProjectTest");
		fixture.whenNewProjectIsSelected();
		fixture.thenProjectHasTheFile("CMakeLists.txt", "/main");
		fixture.thenFileContentsMatchDefaultFile("/main", "CMakeLists.txt");
		fixture.thenProjectHasTheFile(".project", null);
		fixture.thenFileContentsMatchDefaultFile(null, ".project");
	}

	@Test
	public void givenNewIDFProjectIsCreatedAndBuiltThenProjectIsCreatedAndBuilt() throws Exception
	{
		fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		fixture.givenProjectNameIs("NewProjectTest");
		fixture.whenNewProjectIsSelected();
		fixture.whenProjectIsBuilt();
		fixture.thenConsoleShowsBuildSuccessful();
	}

	private class Fixture
	{
		private SWTWorkbenchBot bot;
		private String category;
		private String subCategory;
		private String projectName;

		private Fixture()
		{
			bot = new SWTWorkbenchBot();
		}

		private void givenNewEspressifIDFProjectIsSelected(String category, String subCategory)
		{
			this.category = category;
			this.subCategory = subCategory;
		}

		private void givenProjectNameIs(String projectName)
		{
			this.projectName = projectName;
		}

		private void whenNewProjectIsSelected() throws Exception
		{
			ProjectTestOperations.setupProject(projectName, category, subCategory, bot);
		}

		public void whenProjectIsBuilt() throws IOException
		{
			ProjectTestOperations.buildProject(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(projectName, bot);
		}

		private void thenProjectIsAddedToProjectExplorer()
		{
			bot.viewByTitle("Project Explorer");
			bot.tree().expandNode(projectName).select();
		}

		private void thenProjectHasTheFile(String fileName, String path)
		{
			bot.viewByTitle("Project Explorer");
			String pathToPass = StringUtils.isNotEmpty(path) ? projectName.concat(path) : projectName;
			assertTrue(TestAssertUtility.treeContainsItem(fileName, pathToPass, bot.tree()));
		}

		private void thenFileContentsMatchDefaultFile(String path, String fileName) throws IOException
		{
			bot.viewByTitle("Project Explorer").show();
			String pathToPass = StringUtils.isNotEmpty(path) ? projectName.concat(path) : projectName;
			SWTBotTreeItem[] items = SWTBotTreeOperations.getTreeItems(bot.tree(), pathToPass);
			Optional<SWTBotTreeItem> file = Arrays.asList(items).stream().filter(i -> i.getText().equals(fileName))
					.findFirst();
			String defaultFileContents = DefaultFileContentsReader.getFileContents(pathToPass + "/" + fileName);
			if (file.isPresent())
			{
				file.get().doubleClick();
				SWTBotEditor editor = bot.editorByTitle(fileName);
				editor.show();
				bot.sleep(1000);
				switchEditorToSourceIfPresent(editor);
				assertTrue(editor.toTextEditor().getText().equals(defaultFileContents));
			}
			else
			{
				throw new AssertionError("File not found: " + fileName);
			}
		}

		private void thenConsoleShowsBuildSuccessful()
		{
			SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
			consoleView.show();
			consoleView.setFocus();
			String consoleTextString = consoleView.bot().styledText().getText();
			assertTrue(consoleTextString.contains("Build complete (0 errors"));
		}

		private void cleanTestEnv()
		{
			ProjectTestOperations.closeProject(projectName, bot);
			ProjectTestOperations.deleteProject(projectName, bot);
		}
		
		private void switchEditorToSourceIfPresent(SWTBotEditor editor)
		{
			try
			{
				editor.toTextEditor().bot().cTabItem("Source").activate();
			}
			catch (WidgetNotFoundException e)
			{
				// do nothing 
			}
			
		}
	}
}
