package com.taskapp.logic;

import java.time.LocalDate;
import java.util.List;

import com.taskapp.dataaccess.LogDataAccess;
import com.taskapp.dataaccess.TaskDataAccess;
import com.taskapp.dataaccess.UserDataAccess;
import com.taskapp.exception.AppException;
import com.taskapp.model.Log;
import com.taskapp.model.Task;
import com.taskapp.model.User;

public class TaskLogic {
    private final TaskDataAccess taskDataAccess;
    private final LogDataAccess logDataAccess;
    private final UserDataAccess userDataAccess;

    public TaskLogic() {
        taskDataAccess = new TaskDataAccess();
        logDataAccess = new LogDataAccess();
        userDataAccess = new UserDataAccess();
    }

    /**
     * 自動採点用に必要なコンストラクタのため、皆さんはこのコンストラクタを利用・削除はしないでください
     * 
     * @param taskDataAccess
     * @param logDataAccess
     * @param userDataAccess
     */
    public TaskLogic(TaskDataAccess taskDataAccess, LogDataAccess logDataAccess, UserDataAccess userDataAccess) {
        this.taskDataAccess = taskDataAccess;
        this.logDataAccess = logDataAccess;
        this.userDataAccess = userDataAccess;
    }

    /**
     * 全てのタスクを表示します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findAll()
     * @param loginUser ログインユーザー
     */
    public void showAll(User loginUser) {
        // status の値に応じて表示を変えるようにすること
        // 0→未着手、1→着手中、2→完了
        // タスクを担当するユーザーの名前が表示できるようにすること
        // 担当者が今ログインしてるユーザーなら、「あなたが担当しています」と表示する
        // そうでないなら、担当してるユーザー名を表示する
        List<Task> tasks = taskDataAccess.findAll();
        for (Task task : tasks) {
            String status = "未着手";
            if (task.getStatus() == 1) {
                status = "着手中";
            } else if (task.getStatus() == 2) {
                status = "完了";
            }
            String rep;
            if (task.getRepUser().getCode() == loginUser.getCode()) {
                rep = "あなたが担当しています";
            } else {
                rep = task.getRepUser().getName() + "が担当しています";
            }
            System.out.println(task.getCode() + ". タスク名:" + task.getName() + ", 担当者名:" +
                    rep + ", ステータス:" + status);
        }
    }

    /**
     * 新しいタスクを保存します。
     *
     * @see com.taskapp.dataaccess.UserDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#save(com.taskapp.model.Task)
     * @see com.taskapp.dataaccess.LogDataAccess#save(com.taskapp.model.Log)
     * @param code        タスクコード
     * @param name        タスク名
     * @param repUserCode 担当ユーザーコード
     * @param loginUser   ログインユーザー
     * @throws AppException ユーザーコードが存在しない場合にスローされます
     */
    public void save(int code, String name, int repUserCode,
            User loginUser) throws AppException {
        User user = userDataAccess.findByCode(repUserCode);
        if (user == null) {
            throw new AppException("存在するユーザーコードを入力してください");
        }

        Task task = new Task(code, name, 0, user);
        taskDataAccess.save(task);

        Log log = new Log(code, loginUser.getCode(), 0, LocalDate.now());
        logDataAccess.save(log);

        System.out.println(task.getName() + "の登録が完了しました。");
    }

    /**
     * タスクのステータスを変更します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#update(com.taskapp.model.Task)
     * @see com.taskapp.dataaccess.LogDataAccess#save(com.taskapp.model.Log)
     * @param code      タスクコード
     * @param status    新しいステータス
     * @param loginUser ログインユーザー
     * @throws AppException タスクコードが存在しない、またはステータスが前のステータスより1つ先でない場合にスローされます
     */
    public void changeStatus(int code, int status,
            User loginUser) throws AppException {
        // 以下仕様に沿わない場合、AppExceptionをスローする
        // 入力されたタスクコードが tasks.csvに存在しない場合
        // スローするときのメッセージは「存在するタスクコードを入力してください」とする
        // tasks.csvに存在するタスクのステータスが、変更後のステータスの1つ前じゃない場合
        // 変更可能な例：「未着手」から「着手中」、または「着手中」から「完了」
        // 変更できない例：「未着手」から「完了」、「着手中」から「着手中」、「完了」から他のステータス
        // スローするときのメッセージは「ステータスは、前のステータスより1つ先のもののみを選択してください」とする
        Task task = taskDataAccess.findByCode(code);
        if (task == null) {
            throw new AppException("存在するタスクコードを入力してください");
        }

        if (!((task.getStatus() == 0 && status == 1) || task.getStatus() == 1 && status == 2)) {
            throw new AppException("ステータスは、前のステータスより1つ先のもののみを選択してください");
        }

        // tasks.csvの該当タスクのステータスを変更後のステータスに更新する
        task.setStatus(status);
        taskDataAccess.update(task);

        // logs.csvにデータを1件作成する
        // Statusは変更後のステータス
        // Change_User_Codeは今ログインしてるユーザーコード
        // Change_Dateは今日の日付
        Log log = new Log(code, loginUser.getCode(), status, LocalDate.now());
        logDataAccess.save(log);

        System.out.println("ステータスの変更が完了しました。");
    }

    /**
     * タスクを削除します。
     *
     * @see com.taskapp.dataaccess.TaskDataAccess#findByCode(int)
     * @see com.taskapp.dataaccess.TaskDataAccess#delete(int)
     * @see com.taskapp.dataaccess.LogDataAccess#deleteByTaskCode(int)
     * @param code タスクコード
     * @throws AppException タスクコードが存在しない、またはタスクのステータスが完了でない場合にスローされます
     */
    public void delete(int code) throws AppException {
        // 以下仕様に沿わない場合、AppExceptionをスローする
        // 入力されたタスクコードが tasks.csvに存在しない場合
        // スローするときのメッセージは「存在するタスクコードを入力してください」とする
        // 該当タスクのステータスが、完了（2）ではない場合
        // スローするときのメッセージは「ステータスが完了のタスクを選択してください」とする
        // AppExceptionがスローされたらTaskUI側でメッセージを表示し、再度タスクコードの入力に戻る

        Task task = taskDataAccess.findByCode(code);

        if (task == null) {
            throw new AppException("存在するタスクコードを入力してください");
        }

        if (task.getStatus() != 2) {
            throw new AppException("ステータスが完了のタスクを選択してください");
        }

        // tasks.csvの該当タスクデータを削除すること
        taskDataAccess.delete(code);

        // logs.csvの該当タスクコードがあるデータを全て削除すること
        logDataAccess.deleteByTaskCode(code);

        System.out.println(task.getName() + "の削除が完了しました。");
    }
}