package org.kie.remote.client.api;

import java.util.Map;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.task.query.TaskQueryBuilder;

public interface RemoteTaskService {

    // MAIN TASK OPERATIONS

    RemoteApiResponse activate( long taskId );

    RemoteApiResponse claim( long taskId );

    RemoteApiResponse claimNextAvailable();

    RemoteApiResponse complete( long taskId, Map<String, Object> data );

    RemoteApiResponse delegate( long taskId, String targetUserId );

    RemoteApiResponse exit( long taskId );

    RemoteApiResponse fail( long taskId );

    RemoteApiResponse fail( long taskId, Map<String, Object> faultData );

    RemoteApiResponse forward( long taskId, String targetUserId );

    RemoteApiResponse release( long taskId );

    RemoteApiResponse resume( long taskId );

    RemoteApiResponse skip( long taskId );

    RemoteApiResponse start( long taskId );

    RemoteApiResponse stop( long taskId );

    RemoteApiResponse suspend( long taskId );

    RemoteApiResponse nominate( long taskId, List<String> potentialOwnerUserIds );

    // TASK INFO AND MODIFICATION OPERATIONS

    RemoteApiResponse addGroupToTask( long taskId, String... groupId );

    RemoteApiResponse addUserToTask( long taskId, String... userId );

    RemoteApiResponse setExpirationDate( long taskId, Date date );
    
    RemoteApiResponse<Date> getExpirationDate( long taskId );

    RemoteApiResponse setPriority( long taskId, int priority );
    
    RemoteApiResponse<Integer> getPriority( long taskId );

    RemoteApiResponse addTaskNames( long taskId, String... taskNames );
    
    RemoteApiResponse addTaskNames( long taskId, String language, String... taskNames );

    RemoteApiResponse addDescriptions( long taskId, String... description );
    
    RemoteApiResponse addDescriptions( long taskId, String language, String... description );
    
    RemoteApiResponse<List<String>> getDescriptions( long taskId );
    
    RemoteApiResponse<List<String>> getDescriptions( long taskId, String language );

    RemoteApiResponse setSkipable( long taskId, boolean skipable );
    
    RemoteApiResponse<Boolean> isSkipable( long taskId );

//    RemoteApiResponse setSubTaskStrategy( long taskId, SubTasksStrategy strategy ); // kie-internal -> kie-api: expose subtask strategy

    // CONTENT OPERATIONS

    RemoteApiResponse<Long> addOutputContent( long taskId, Map<String, Object> params );
    
    RemoteApiResponse<Map<String, Object>> getOutputContentMap( long taskId );
    
    RemoteApiResponse<byte[]> getContent( long taskId );
    
    <T> RemoteApiResponse<T> getContent( long taskId, Class<T> contentType );
    
    RemoteApiResponse<byte[]> Content getContentById( long contentId );
    
    <T> RemoteApiResponse<T> getContentByid( long contentId, Class<T> contentType );

   RemoteApiResponse<Long> addContent( long taskId, Object contentObject );

   RemoteApiResponse<Long> addContent( long taskId, Map<String, Object> params );

    RemoteApiResponse deleteContent( long taskId, long contentId );

    RemoteApiResponse<List<Object>> getAllContentByTaskId( long taskId );

    Map<String, Object> getTaskContentMap( long taskId );
    
    // ATTACHMENT OPERATIONS
    
    Attachment getAttachmentById( long attachId );
    
   RemoteApiResponse<Long> addAttachment( long taskId, String attachmentName, String attachmentType, Object attachmentContentObject );

    RemoteApiResponse deleteAttachment( long taskId, long attachmentId );

    List<Attachment> getAllAttachments( long taskId );

    // COMMENT OPERATIONS
    
    RemoteApiResponse<Long> addComment( long taskId, String commentText );

    RemoteApiResponse deleteComment( long taskId, long commentId );

    RemoteApiResponse<List<String>> getAllComments( long taskId );

    RemoteApiResponse<String> getCommentById( long commentId );

    // FAULT OPERATIONS
    
    RemoteApiResponse deleteFault( long taskId );
    
    RemoteApiResponse setFault( long taskId, String faultName, Object faultData );

    // OUTPUT OPERATIONS
    
    RemoteApiResponse deleteOutput( long taskId );

    RemoteApiResponse setOutput( long taskId, Object outputData );

    // QUERY AND GET OPERATIONS

    TaskQueryBuilder taskQuery( String userId );

    RemoteApiResponse<List<TaskSummary>> getTasksAssignedAsBusinessAdministratorByStatus( String userId, String language, List<Status> statuses );

    RemoteApiResponse<List<TaskSummary>> getTasksAssignedByGroup( String... groupId );

    RemoteApiResponse<List<TaskSummary>> getTasksOwned( List<Status> status);

    RemoteApiResponse<List<TaskSummary>> getActiveTasks();

    RemoteApiResponse<List<TaskSummary>> getActiveTasks( Date since );

    RemoteApiResponse<List<TaskSummary>> getArchivedTasks();

    RemoteApiResponse<List<TaskSummary>> getCompletedTasks();

    RemoteApiResponse<List<TaskSummary>> getCompletedTasks( Date since );

    RemoteApiResponse<List<TaskSummary>> getSubTasksByParent( long... parenTasktId );

}
