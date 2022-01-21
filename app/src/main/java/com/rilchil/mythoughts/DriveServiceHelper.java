package com.rilchil.mythoughts;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {

    private final Executor executor = Executors.newSingleThreadExecutor();
    static final String thoughtsDBDriveFileName = "thoughtsDB";
    private Drive driveService;
    private SQLiteDatabase database;

    public DriveServiceHelper(Drive drive){
        driveService = drive;
    }

    public void setDatabase(SQLiteDatabase database) {
        this.database = database;
    }



    public Task<String> createDriveCSVFile(String filePath){
        return Tasks.call(executor,() -> {
            //check if file exists
            FileList result = driveService.files().list()
                    .setQ("name = 'thoughtsDB'")
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name,createdTime)")
                    .execute();
            boolean exists = result.getFiles().size() > 0;
            if(exists){
                String fileId = result.getFiles().get(0).getId();
                File file = new File();
                file.setName(thoughtsDBDriveFileName);
                java.io.File fileContent = new java.io.File(filePath);
                FileContent mediaContent = new FileContent("text/csv", fileContent);
                //File updatedFile = null;

                try {
                    driveService.files().update(fileId,file,mediaContent).execute();
                    return "Backup Complete";
                } catch (IOException e) {
                    e.printStackTrace();
                    return e.toString();
                }

            } else {
                File metaData = new File();
                metaData.setName(thoughtsDBDriveFileName);
                metaData.setParents(Collections.singletonList("appDataFolder"));


                java.io.File file = new java.io.File(filePath);

                FileContent fileContent = new FileContent("text/csv", file);
                File myFile = null;

                try {
                    myFile = driveService.files().create(metaData, fileContent).execute();
                    return "Backup Complete";
                } catch (IOException e) {
                    e.printStackTrace();
                    return e.toString();
                }
            }


        });

        /*
            FileList result = driveService.files().list()
                    .setQ("name = 'ThoughtsDB'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name,createdTime)")
                    .execute();
            StringBuilder stringBuilder = new StringBuilder();
            for (File f : result.getFiles()) {
                try{
                    driveService.files().delete(f.getId()).execute();
                    stringBuilder.append(f.getName()).append(f.getCreatedTime()).append(" deleted");
                } catch (IOException e){
                    stringBuilder.append(f.getName()).append(" could not be deleted");
                }

            }
                        return stringBuilder.toString();


             */


    }

    public Task<String> downloadCSVFile(String outputPath){
        return Tasks.call(executor,() -> {
            try {
                FileList result = driveService.files().list()
                        .setQ("name = 'thoughtsDB'")
                        .setSpaces("appDataFolder")
                        .setFields("nextPageToken, files(id, name,createdTime)")
                        .execute();
                if (result.getFiles().size() > 0) {
                    File file = result.getFiles().get(0);
                    String fileId = file.getId();
                    InputStream inputStream = driveService.files().get(fileId).executeMediaAsInputStream();
                    CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream));
                    String[] titles = csvReader.readNext();
                    String[] row;
                    database.delete(ThoughtTable.TABLE_NAME,null,null);
                    while ((row = csvReader.readNext()) != null) {
                        try {
                            String title = row[1];
                            String thought = row[2];
                            String date = row[3];
                            String time = row[4];
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(ThoughtTable.COLUMN_TITLE, title);
                            contentValues.put(ThoughtTable.COLUMN_THOUGHT_TEXT, thought);
                            contentValues.put(ThoughtTable.COLUMN_DATE, date);
                            contentValues.put(ThoughtTable.COLUMN_TIME, time);
                            database.insert(ThoughtTable.TABLE_NAME, null, contentValues);

                        } catch (IndexOutOfBoundsException e){
                            return e.toString();
                        }

                    }
                    database.close();
                    return "Restore Complete";

                } else {
                    return "No Backup files found";
                }
            }catch (IOException e){
                return e.toString();
            }
        });

    }

    public Task<String> getMostRecentBackupDetails(){
        return Tasks.call(executor,() -> {
            FileList result = driveService.files().list()
                    .setQ("name = 'thoughtsDB'")
                    .setSpaces("appDataFolder")
                    .setFields("nextPageToken, files(id, name,modifiedTime,mimeType)")
                    .execute();
            //if theres no backup file then dont do anything
            if(result.getFiles().size() > 0){
                File file = result.getFiles().get(0);
                String isoTime = file.getModifiedTime().toString();
                DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = utcFormat.parse(isoTime);

                DateFormat dateFormat = DateFormat.getDateInstance();
                dateFormat.setTimeZone(TimeZone.getDefault());

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                simpleDateFormat.setTimeZone(TimeZone.getDefault());


                return simpleDateFormat.format(date);
            } else {
                return null;
            }
        });
    }



}
