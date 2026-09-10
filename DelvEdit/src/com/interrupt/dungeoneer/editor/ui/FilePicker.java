package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.ui.menu.FileListItem;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class FilePicker extends Dialog {

    private static final Color EDITOR_ACCENT_COLOR =
        new Color(0.30f, 0.70f, 1.00f, 1.00f);

    private String lastClickedEntry = null;
    private long lastClickTime = 0L;

    private static final long DOUBLE_CLICK_TIME_MS = 350L;

    public interface ResultListener {
        boolean result(boolean success, FileHandle result);
    }

    private final Skin skin;
    private boolean fileNameEnabled;
    private boolean newFolderEnabled;
    private boolean directoryBrowsingEnabled = true;
    private boolean legacyFormatDisplayToggleEnabled;
    private final TextField fileNameInput;
    private final Label fileNameLabel;
    private final TextButton newFolderButton;
    protected final FileHandle baseDir;
    private final Label fileListLabel;
    private final List fileList;
    private final CheckBox legacyFormatDisplayToggle;
    private ChangeListener legacyFormatDisplayToggleChangeListener;

    private FileHandle currentDir;
    protected String result;

    protected ResultListener resultListener;

    private Stage stage;

    private final TextButton ok;
    private final TextButton cancel;

    private static final Comparator<FileListItem> dirListComparator = new Comparator<FileListItem>() {
        @Override
        public int compare(FileListItem file1, FileListItem file2) {
            if (file1.file.isDirectory() && !file2.file.isDirectory()) {
                return -1;
            }
            if (file1.file.isDirectory() && file2.file.isDirectory()) {
                return 0;
            }
            if (!file1.file.isDirectory() && !file2.file.isDirectory()) {
                return 0;
            }
            return 1;
        }
    };

    private final FileFilter defaultFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return true;
        }
    };
    private FileFilter filter = defaultFilter;

    public FilePicker(String title, final Skin skin, FileHandle baseDir) {
        super(title, skin);
        this.skin = skin;
        this.baseDir = baseDir;
        currentDir = this.baseDir;

        final Table content = getContentTable();
        content.top().left();

        fileListLabel = new Label("", skin);
        fileListLabel.setAlignment(Align.left);
        fileListLabel.setColor(EDITOR_ACCENT_COLOR);

        fileList = new List(skin);
        fileList.setItems(currentDir.list());

        fileNameInput = new TextField("", skin);
        fileNameLabel = new Label("File name", skin);
        fileNameLabel.setColor(0.82f, 0.84f, 0.87f, 1f);
        fileNameInput.setTextFieldListener(new TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                result = textField.getText();
            }
        });

        legacyFormatDisplayToggle = new CheckBox("Display legacy formats", skin);

        newFolderButton = new TextButton("New Folder", skin);

        newFolderButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {

                if (newFolderButton.isChecked()) {
                    newFolderButton.setChecked(false);
                    /*new NewFileDialog("New Folder", skin) {
                        @Override
                        protected void result(Object object) {
                            final boolean success = (Boolean) object;
                            if (success) {
                                final FileHandle newFolder = new FileHandle(currentDir.path() + "/" + getResult());
                                newFolder.mkdirs();
                                changeDirectory(currentDir);
                            }
                        };
                    }.show(stage);*/
                }
            }
        });

        ok = new TextButton("Ok", skin);
        button(ok, true);

        cancel = new TextButton("Cancel", skin);
        button(cancel, false);
        key(Keys.ENTER, true);
        key(Keys.ESCAPE, false);

        fileList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                FileHandle[] items = currentDir.list();
                FileHandle selected = null;

                String selectedName = ((FileListItem)fileList.getSelected()).toString().replace("/", "");

                for(int i = 0; i < items.length; i++) {
                    String fh = items[i].file().getName();
                    if(fh.equals(selectedName)) {
                        selected = items[i];
                    }
                }

                if (selected != null && !selected.isDirectory()) {
                    result = selected.name();
                    fileNameInput.setText(result);
                }
            }
        });
    }

    private void changeDirectory(FileHandle directory) {
        currentDir = directory;
        String title = currentDir.file().getAbsolutePath();
        fileListLabel.setText(title);

        final Array<FileListItem> items = new Array<FileListItem>();

        final FileHandle[] list = directory.list();
        for (final FileHandle handle : list) {
            if (handle.file().isHidden()) continue;
            if (filter.accept(handle.file()) || handle.isDirectory()) {
                items.add(new FileListItem(handle));
            }
        }

        items.sort(dirListComparator);

        FileHandle fh = new FileHandle(directory.file().getAbsolutePath());
        if (fh.parent() != null) {
            items.insert(0, new FileListItem("..", fh.parent()));
        }

        items.shrink();

        fileList.setItems(items.items);
        fileList.setSelected(fileNameInput.getText());
    }

    public FileHandle getResult() {
        String path = currentDir.path() + "/";
        if (result != null && result.length() > 0) {
            path += result;
        }

        Path cleanPath = Paths.get(path).normalize();

        return new FileHandle(cleanPath.toString());
    }

    public FilePicker setFilter(FileFilter filter) {
        this.filter = filter;
        return this;
    }

    public FilePicker setOkButtonText(String text) {
        this.ok.setText(text);
        return this;
    }


    public FilePicker setCancelButtonText(String text) {
        this.cancel.setText(text);
        return this;
    }

    public FilePicker setFileNameEnabled(boolean fileNameEnabled) {
        this.fileNameEnabled = fileNameEnabled;
        return this;
    }

    public FilePicker setFileName(String fileName) {
        fileNameInput.setText(fileName);
        result = fileName;
        fileList.setSelected(fileName);
        return this;
    }

    public FilePicker setNewFolderEnabled(boolean newFolderEnabled) {
        this.newFolderEnabled = newFolderEnabled;
        return this;
    }

    public FilePicker setResultListener(ResultListener result) {
        this.resultListener = result;
        return this;
    }


    public FilePicker disableDirectoryBrowsing() {
        this.directoryBrowsingEnabled = false;
        return this;
    }

    public FilePicker enableLegacyFormatDisplayToggle(final FileFilter defaultFileFilter, final FileFilter legacyFileFilter) {
        legacyFormatDisplayToggleEnabled = true;

        if (legacyFormatDisplayToggleChangeListener != null) {
            legacyFormatDisplayToggle.removeListener(legacyFormatDisplayToggleChangeListener);
            legacyFormatDisplayToggleChangeListener = null;
        }

        legacyFormatDisplayToggleChangeListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                filter = legacyFormatDisplayToggle.isChecked() ? legacyFileFilter : defaultFileFilter;
                changeDirectory(currentDir);
            }
        };

        legacyFormatDisplayToggle.addListener(legacyFormatDisplayToggleChangeListener);

        return this;
    }

    public FilePicker disableLegacyFormatDisplayToggle() {
        legacyFormatDisplayToggleEnabled = false;

        if (legacyFormatDisplayToggleChangeListener != null) {
            legacyFormatDisplayToggle.removeListener(legacyFormatDisplayToggleChangeListener);
            legacyFormatDisplayToggleChangeListener = null;
        }

        return this;
    }

    @Override
    public Dialog show(Stage stage) {
        final Table content = getContentTable();
        content.add(fileListLabel)
            .colspan(2)
            .left()
            .expandX()
            .fillX()
            .padLeft(14f)
            .padRight(14f)
            .padTop(12f)
            .padBottom(10f)
            .row();

        ScrollPane pane = new ScrollPane(fileList, skin);
        pane.addListener(new InputListener() {
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Stage stage = getStage();
                if (stage != null) {
                    stage.setScrollFocus(fromActor);
                }
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Stage stage = getStage();
                if (stage != null) {
                    stage.setScrollFocus(null);
                }
            }
        });

        content.add(pane)
            .width(520f)
            .height(420f)
            .colspan(2)
            .fill()
            .expand()
            .padLeft(14f)
            .padRight(14f)
            .padBottom(10f)
            .row();

        if (legacyFormatDisplayToggleEnabled) {
            content.add(legacyFormatDisplayToggle)
                .left()
                .colspan(2)
                .padLeft(14f)
                .padRight(14f)
                .padTop(4f)
                .padBottom(10f)
                .row();
        }

        if (fileNameEnabled) {
            content.add(fileNameLabel)
                .left()
                .width(100f)
                .padLeft(14f)
                .padBottom(10f);

            content.add(fileNameInput)
                .fillX()
                .expandX()
                .height(34f)
                .padRight(14f)
                .padBottom(10f)
                .row();
        }

        if(directoryBrowsingEnabled){
            fileList.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {

                    if (getTapCount() < 1) {
                        return;
                    }

                    FileListItem selectedItem = (FileListItem) fileList.getSelected();

                    if (selectedItem == null) {
                        return;
                    }

                    String selection = selectedItem.toString();
                    // Only treat it as a double-click if both clicks were
                    // on the exact same file/folder entry.
                    if (getTapCount() < 2 || !selection.equals(lastClickedEntry)) {
                        lastClickedEntry = selection;
                        return;
                    }

                    // Successful double-click. Reset so the next rapid click
                    // cannot accidentally open another highlighted entry.
                    lastClickedEntry = null;

                    if (selection.equals("../")) {
                        FileHandle parent = currentDir.parent();

                        if (parent != null) {
                            changeDirectory(parent);
                        }

                        return;
                    }

                    FileHandle[] items = currentDir.list();

                    String selectedName = selection.replace("/", "");

                    for (FileHandle item : items) {
                        if (item.name().equals(selectedName)) {

                            if (item.isDirectory()) {
                                changeDirectory(item);
                            }

                            break;
                        }
                    }
                }
            });
        }

        this.stage = stage;

        File file = null;
        try {
            file = baseDir.file().getCanonicalFile();
        } catch (IOException e) {
            file = baseDir.file().getAbsoluteFile();
        }
        changeDirectory(new FileHandle(file));

        return super.show(stage);
    }

    public static FilePicker createSaveDialog(String title, final Skin skin, final FileHandle path) {
        final FilePicker save = new FilePicker(title, skin, path) {
            @Override
            protected void result(Object object) {

                if (resultListener == null) {
                    return;
                }

                final boolean success = (Boolean) object;
                if (!resultListener.result(success, getResult())) {
                    this.cancel();
                }
            }
        }.setFileNameEnabled(true).setNewFolderEnabled(true).setOkButtonText("Save");

        return save;

    }

    public static FilePicker createLoadDialog(String title, final Skin skin, final FileHandle path) {
        final FilePicker load = new FilePicker(title, skin, path) {
            @Override
            protected void result(Object object) {

                if (resultListener == null) {
                    return;
                }

                final boolean success = (Boolean) object;
                resultListener.result(success, getResult());
            }
        }.setNewFolderEnabled(false).setFileNameEnabled(false).setOkButtonText("Load");

        return load;

    }

    public static FilePicker createPickDialog(String title, final Skin skin, final FileHandle path) {
        final FilePicker pick = new FilePicker(title, skin, path) {
            @Override
            protected void result(Object object) {

                if (resultListener == null) {
                    return;
                }

                final boolean success = (Boolean) object;
                resultListener.result(success, getResult());
            }
        }.setOkButtonText("Select");

        return pick;
    }

}
