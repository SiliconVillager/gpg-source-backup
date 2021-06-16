//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include "facebook/FacebookServiceStates.h"
#include <QtGui/QMainWindow>

//==============================================================================
// Forward Declarations
//==============================================================================

class FacebookService;
namespace Ui
{
  class MainWindow;
}

//==============================================================================
// Declarations
//==============================================================================

/** Facebook Gems8 demo. */
class MainWindow : public QMainWindow
{
  Q_OBJECT

public:

  /**
   * Creates a new application window.
   * @param parent UI parent for the window.
   */
  MainWindow(QWidget* parent = 0);

  /** Destructor. */
  ~MainWindow();

public slots:

  /** Called when the login button was pressed. */
  void loginButtonClicked();

  /** Called when the refresh button was pressed. */
  void refreshFriendsButtonClicked();

  /** Called when the refresh button was pressed. */
  void refreshAppUsersButtonClicked();

  /** Called when the send button was pressed. */
  void sendMessageButtonClicked();

private:

  /** Login state. */
  enum LoginState
  {
    LS_LoggedIn,              ///< Logged in.
    LS_LoggedOut,             ///< Not logged in.
    LS_LoginInProgress,       ///< Waiting for login in external browser.
    LS_PermissionInProgress   ///< Waiting for permission confirmation.
  };

  /**
   * Updates the UI to reflect the current login state.
   * @param state login state.
   */
  void setLoginState(LoginState state);

  /**
   * Returns the current login state.
   * @return state.
   */
  LoginState loginState() const;

  /** Updates the UI to show that a request is in progress. */
  void startRequest();

  /** Updates the UI to show that a request has finished. */
  void endRequest(FacebookServiceState state);

  /**
   * Persists a Facebook session as part of the application's settings.
   * @param sessionKey session key.
   * @param sessionSecret session secret.
   * @param uid user ID.
   */
  void storeFacebookSession(QString sessionKey, QString sessionSecret,
    QString uid) const;

  /**
   * Reads the Facebook session from disk.
   * @param sessionKey session key.
   * @param sessionSecret session secret.
   * @param uid user ID.
   * @return true if there was a session persisted; false otherwise.
   */
  bool hasFacebookSession(QString& sessionKey, QString& sessionSecret,
    QString& uid) const;

private:

  Ui::MainWindow* m_Ui;                 ///< User interface.
  FacebookService* m_FacebookService;   ///< Access to Facebook service.
  LoginState m_LoginState;              ///< Current login state.
};

#endif // MAINWINDOW_H
