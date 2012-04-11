/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package git4idea.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.changes.ui.SelectFilesDialog;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.PlatformFacade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Kirill Likhodedov
 */
public class UntrackedFilesNotifier {

  private UntrackedFilesNotifier() {
  }

  /**
   * Displays notification about {@code untracked files would be overwritten by checkout} error.
   * Clicking on the link in the notification opens a simple dialog with the list of these files.
   * @param operation   the name of the Git operation that caused the error: {@code rebase, merge, checkout}.
   * @param description the content of the notification or null if the deafult content is to be used.
   */
  public static void notifyUntrackedFilesOverwrittenBy(@NotNull final Project project, @NotNull PlatformFacade platformFacade,
                                                       @NotNull final Collection<VirtualFile> untrackedFiles,
                                                       @NotNull final String operation, @Nullable String description) {
    final String notificationTitle = StringUtil.capitalize(operation) + " error";
    final String notificationDesc = description == null ? createUntrackedFilesOverwrittenDescription(operation, false) : description;
    final String dialogDesc = createUntrackedFilesOverwrittenDescription(operation, true);

    platformFacade.getNotificator(project).notifyError(notificationTitle, notificationDesc,
                                                  new NotificationListener() {
      @Override
      public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SelectFilesDialog dlg = new SelectFilesDialog(project, new ArrayList<VirtualFile>(untrackedFiles),
                                                        StringUtil.stripHtml(dialogDesc, true), null, false, false) {
            @Override
            protected Action[] createActions() {
              return new Action[]{getOKAction()};
            }
          };
          dlg.setTitle("Untracked Files Preventing " + StringUtil.capitalize(operation));
          dlg.show();
        }
      }
    });
  }
  
  public static String createUntrackedFilesOverwrittenDescription(@NotNull final String operation, boolean filesAreShown) {
    final String description1 = " untracked working tree files would be overwritten by " + operation + ".";
    final String description2 = "Please move or remove them before you can " + operation + ".";
    final String notificationDesc;
    if (filesAreShown) {
      notificationDesc = "These" + description1 + "<br/>" + description2;
    }
    else {
      notificationDesc = "Some" + description1 + "<br/>" + description2 + " <a href='view'>View them</a>";
    }
    return notificationDesc;
  }
}
