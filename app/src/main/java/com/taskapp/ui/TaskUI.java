package com.taskapp.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.taskapp.exception.AppException;
import com.taskapp.logic.TaskLogic;
import com.taskapp.logic.UserLogic;
import com.taskapp.model.User;

public class TaskUI {
    private final BufferedReader reader;

    private final UserLogic userLogic;

    private final TaskLogic taskLogic;

    private User loginUser;

    public TaskUI() {
        reader = new BufferedReader(new InputStreamReader(System.in));
        userLogic = new UserLogic();
        taskLogic = new TaskLogic();
    }

    /**
     * 自動採点用に必要なコンストラクタのため、皆さんはこのコンストラクタを利用・削除はしないでください
     * 
     * @param reader
     * @param userLogic
     * @param taskLogic
     */
    public TaskUI(BufferedReader reader, UserLogic userLogic, TaskLogic taskLogic) {
        this.reader = reader;
        this.userLogic = userLogic;
        this.taskLogic = taskLogic;
    }

    /**
     * メニューを表示し、ユーザーの入力に基づいてアクションを実行します。
     *
     * @see #inputLogin()
     * @see com.taskapp.logic.TaskLogic#showAll(User)
     * @see #selectSubMenu()
     * @see #inputNewInformation()
     */
    public void displayMenu() {
        System.out.println("タスク管理アプリケーションにようこそ!!");

        inputLogin();

        // メインメニュー
        boolean flg = true;
        while (flg) {
            try {
                System.out.println("以下1~3のメニューから好きな選択肢を選んでください。");
                System.out.println("1. タスク一覧, 2. タスク新規登録, 3. ログアウト");
                System.out.print("選択肢：");
                String selectMenu = reader.readLine();

                System.out.println();

                switch (selectMenu) {
                    case "1":
                        // タスク一覧
                        taskLogic.showAll(loginUser);
                        // タスク一覧表示後に、ステータス更新機能を選択できるサブメニューを追加
                        selectSubMenu();
                        break;
                    case "2":
                        // タスク新規登録
                        inputNewInformation();
                        break;
                    case "3":
                        System.out.println("ログアウトしました。");
                        flg = false;
                        break;
                    default:
                        System.out.println("選択肢が誤っています。1~3の中から選択してください。");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println();
        }
    }

    /**
     * ユーザーからのログイン情報を受け取り、ログイン処理を行います。
     *
     * @see com.taskapp.logic.UserLogic#login(String, String)
     */
    public void inputLogin() {
        // メールアドレスとパスワードを入力させる
        // メールアドレスを入力させるときは「メールアドレスを入力してください：」というメッセージを表示
        // パスワードを入力させるときは「パスワードを入力してください：」というメッセージを表示
        // 入力された値に合致するデータを users.csvの中から探す
        // 合致するデータが見つかった場合は、メニューの一覧が表示される
        // 合致するデータが見つからなかった場合は、AppExceptionをスローする
        // スローするときのメッセージは「既に登録されているメールアドレス、パスワードを入力してください」とする
        // AppExceptionがスローされたらTaskUI側でメッセージを表示し、再度メールアドレスの入力に戻る
        boolean flg = true;
        while (flg) {
            try {
                System.out.print("メールアドレスを入力してください：");
                String email = reader.readLine();
                System.out.print("パスワードを入力してください：");
                String password = reader.readLine();
                loginUser = userLogic.login(email, password);
                flg = false;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AppException e) {
                System.out.println(e.getMessage());
            }
            System.out.println();
        }
    }

    /**
     * ユーザーからの新規タスク情報を受け取り、新規タスクを登録します。
     *
     * @see #isNumeric(String)
     * @see com.taskapp.logic.TaskLogic#save(int, String, int, User)
     */
    public void inputNewInformation() {
        // タスクコード、タスク名、タスクを担当するユーザーコードを入力させる
        // タスクコードを入力させるときは「タスクコードを入力してください：」と表示する
        // タスク名を入力させるときは「タスク名を入力してください：」と表示する
        // 担当するユーザーコードを入力させるときは「担当するユーザーのコードを選択してください：」と表示する
        // 入力値に対して以下の仕様でバリデーションを行うこと
        // タスクコードは数字か
        // 仕様を満たさない場合、「コードは半角の数字で入力してください」と表示し再度タスクコードの入力に戻る
        // タスク名は10文字以内か
        // 仕様を満たさない場合、「タスク名は10文字以内で入力してください」と表示し再度タスクコードの入力に戻る
        // 担当するユーザーコードは数字か
        // 仕様を満たさない場合、「ユーザーのコードは半角の数字で入力してください」と表示し再度タスクコードの入力に戻る
        boolean flg = true;
        while (flg) {
            try {
                System.out.print("タスクコードを入力してください：");
                String taskCode = reader.readLine();
                if (!isNumeric(taskCode)) {
                    System.out.println("コードは半角の数字で入力してください");
                    System.out.println();
                    continue;
                }

                System.out.print("タスク名を入力してください：");
                String taskName = reader.readLine();
                if (!(taskName.length() <= 10)) {
                    System.out.println("タスク名は10文字以内で入力してください");
                    System.out.println();
                    continue;
                }

                System.out.print("担当するユーザーのコードを選択してください：");
                String userCode = reader.readLine();
                if (!isNumeric(userCode)) {
                    System.out.println("ユーザーのコードは半角の数字で入力してください");
                    System.out.println();
                    continue;
                }

                taskLogic.save(Integer.parseInt(taskCode), taskName, Integer.parseInt(userCode), loginUser);
                flg = false;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AppException e) {
                System.out.println(e.getMessage());
            }
            System.out.println();
        }
    }

    /**
     * タスクのステータス変更または削除を選択するサブメニューを表示します。
     *
     * @see #inputChangeInformation()
     * @see #inputDeleteInformation()
     */
    public void selectSubMenu() {
        // サブメニューにて1を選択した場合、以下ステータス更新機能を実行し、2を選択した場合メインメニューの選択に戻る
        boolean flg = true;
        while (flg) {
            try {
                // 設問4にて実装したサブメニューに、削除用の選択肢を追加する
                System.out.println("以下1~3から好きな選択肢を選んでください。");
                System.out.println("1. タスクのステータス変更, 2. タスク削除, 3. メインメニューに戻る");
                System.out.print("選択肢:");
                String selectMenu = reader.readLine();
                System.out.println();

                switch (selectMenu) {
                    case "1":
                        // ステータス更新
                        inputChangeInformation();
                        break;
                    case "2":
                        // タスクの削除
                        inputDeleteInformation();
                        break;
                    case "3":
                        System.out.println("メインメニューに戻ります");
                        flg = false;
                        break;
                    default:
                        System.out.println("選択肢は1~3の中から選択してください");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ユーザーからのタスクステータス変更情報を受け取り、タスクのステータスを変更します。
     *
     * @see #isNumeric(String)
     * @see com.taskapp.logic.TaskLogic#changeStatus(int, int, User)
     */
    public void inputChangeInformation() {
        // ステータスを変更するタスクコード、変更後のステータスを入力させること
        // タスクコードを入力させるときは「ステータスを変更するタスクコードを入力してください：」と表示する
        boolean flg = true;
        while (flg) {
            try {
                System.out.print("ステータスを変更するタスクコードを入力してください：");
                String taskCode = reader.readLine();
                if (!isNumeric(taskCode)) {
                    System.out.println("コードは半角の数字で入力してください");
                    System.out.println();
                    continue;
                }

                System.out.println("どのステータスに変更するか選択してください。");
                System.out.println("1. 着手中, 2. 完了");
                System.out.print("選択肢：");
                String status = reader.readLine();
                if (!isNumeric(status)) {
                    System.out.println("ステータスは半角の数字で入力してください");
                    System.out.println();
                    continue;
                } else if (!(status.equals("1") || status.equals("2"))) {
                    System.out.println("ステータスは1・2の中から選択してください");
                    System.out.println();
                    continue;
                }

                taskLogic.changeStatus(Integer.parseInt(taskCode), Integer.parseInt(status), loginUser);
                flg = false;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AppException e) {
                System.out.println(e.getMessage());
            }
            System.out.println();
        }
    }

    /**
     * ユーザーからのタスク削除情報を受け取り、タスクを削除します。
     *
     * @see #isNumeric(String)
     * @see com.taskapp.logic.TaskLogic#delete(int)
     */
    public void inputDeleteInformation() {
        // 削除するタスクコード入力させること
        // タスクコードを入力させるときは「削除するタスクコードを入力してください：」と表示する
        // 入力値に対して以下の仕様でバリデーションを行うこと
        // タスクコードは数字か
        // 仕様を満たさない場合、「コードは半角の数字で入力してください」と表示し再度タスクコードの入力に戻る
        boolean flg = true;
        while (flg) {
            try {
                System.out.print("削除するタスクコードを入力してください：");
                String taskCode = reader.readLine();
                if (!isNumeric(taskCode)) {
                    System.out.println("コードは半角の数字で入力してください");
                    System.out.println();
                    continue;
                }

                taskLogic.delete(Integer.parseInt(taskCode));
                flg = false;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AppException e) {
                System.out.println(e.getMessage());
            }
            System.out.println();
        }
    }

    /**
     * 指定された文字列が数値であるかどうかを判定します。
     * 負の数は判定対象外とする。
     *
     * @param inputText 判定する文字列
     * @return 数値であればtrue、そうでなければfalse
     */
    public boolean isNumeric(String inputText) {
        return inputText.chars().allMatch(c -> Character.isDigit((char) c));
    }
}