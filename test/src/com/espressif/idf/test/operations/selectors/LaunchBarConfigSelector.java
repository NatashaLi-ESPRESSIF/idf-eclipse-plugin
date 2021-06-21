/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.test.operations.selectors;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;

import org.eclipse.launchbar.ui.controls.internal.CSelector;
import org.eclipse.launchbar.ui.controls.internal.ConfigSelector;
import org.eclipse.launchbar.ui.controls.internal.LaunchBarWidgetIds;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.SWTBotWidget;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

/**
 * Class to interact with CDT LaunchBar to select configs
 * 
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
@SWTBotWidget(clasz = CSelector.class, preferredName = "cselector")
public class LaunchBarConfigSelector extends AbstractSWTBotControl<CSelector>
{
	public LaunchBarConfigSelector(ConfigSelector w) throws WidgetNotFoundException
	{
		super(w);
	}

	public LaunchBarConfigSelector(SWTBot bot)
	{
		this(bot.widget(WidgetMatcherFactory.withId(LaunchBarWidgetIds.CONFIG_SELECTOR)));
	}

	public SWTBot bot()
	{
		return new SWTBot(widget);
	}

	public void click(int x, int y)
	{
		notify(SWT.MouseEnter);
		notify(SWT.MouseMove);
		notify(SWT.Activate);
		notify(SWT.FocusIn);
		notify(SWT.MouseDown, createMouseEvent(x, y, 1, SWT.NONE, 1));
		notify(SWT.MouseUp, createMouseEvent(x, y, 1, SWT.BUTTON1, 1));
	}

	@Override
	public LaunchBarConfigSelector click()
	{
		Point size = syncExec((Result<Point>) () -> widget.getSize());
		click(size.x / 2, size.y / 2);
		return this;
	}

	public void clickEdit()
	{
		bot().buttonWithId(LaunchBarWidgetIds.EDIT).click(); // $NON-NLS-1$
	}

	private void clickOnInternalWidget(int x, int y, Widget internalWidget)
	{
		notify(SWT.MouseDown, createMouseEvent(x, y, 1, SWT.NONE, 1), internalWidget);
		notify(SWT.MouseUp, createMouseEvent(x, y, 1, SWT.BUTTON1, 1), internalWidget);
	}

	public LaunchBarConfigSelector select(String text)
	{
		click();
		Label itemToSelect = bot().shellWithId(LaunchBarWidgetIds.POPUP).bot().widget(withText(text));
		Point itemToSelectLocation = syncExec((Result<Point>) () -> itemToSelect.getLocation());
		clickOnInternalWidget(itemToSelectLocation.x, itemToSelectLocation.y, itemToSelect);
		return this;
	}

	public NewConfigDialog newConfigDialog()
	{
		click();
		new ActionArea(bot().shellWithId(LaunchBarWidgetIds.POPUP)).click();
		return new NewConfigDialog(bot().shell("Create Launch Configuration").widget);
	}

	private class ActionArea extends AbstractSWTBotControl<Composite>
	{
		public ActionArea(SWTBotShell shell) throws WidgetNotFoundException
		{
			super(shell.bot().widget(WidgetMatcherFactory.withId(LaunchBarWidgetIds.NEW)));
		}

		public void click(int x, int y)
		{
			notify(SWT.MouseEnter);
			notify(SWT.MouseMove);
			notify(SWT.Activate);
			notify(SWT.FocusIn);
			notify(SWT.MouseDown, createMouseEvent(x, y, 1, SWT.NONE, 1));
			notify(SWT.MouseUp, createMouseEvent(x, y, 1, SWT.BUTTON1, 1));
		}

		@Override
		public ActionArea click()
		{
			Point size = syncExec((Result<Point>) () -> widget.getSize());
			click(size.x / 2, size.y / 2);
			return this;
		}
	}

	public static class NewConfigDialog extends SWTBotShell
	{
		public NewConfigDialog(Shell shell)
		{
			super(shell);
		}

		public NewConfigDialog setMode(String mode)
		{
			bot().tableInGroup("Launch Mode").select(mode);
			return this;
		}

		public NewConfigDialog setType(String type)
		{
			bot().tableInGroup("Launch Configuration Type").select(type);
			return this;
		}

		public NewConfigDialog next()
		{
			bot().button("Next >").click();
			return this;
		}

		public NewConfigDialog finish()
		{
			bot().button("Finish").click();
			return this;
		}
	}
}
