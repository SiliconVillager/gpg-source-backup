/********************************************************************************
** Form generated from reading ui file 'mainwindow.ui'
**
** Created: Thu 1. Oct 13:21:44 2009
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

#ifndef UI_MAINWINDOW_H
#define UI_MAINWINDOW_H

#include <QtCore/QVariant>
#include <QtGui/QAction>
#include <QtGui/QApplication>
#include <QtGui/QButtonGroup>
#include <QtGui/QCheckBox>
#include <QtGui/QFrame>
#include <QtGui/QGridLayout>
#include <QtGui/QHBoxLayout>
#include <QtGui/QHeaderView>
#include <QtGui/QLabel>
#include <QtGui/QLineEdit>
#include <QtGui/QMainWindow>
#include <QtGui/QMenuBar>
#include <QtGui/QProgressBar>
#include <QtGui/QPushButton>
#include <QtGui/QStatusBar>
#include <QtGui/QWidget>

QT_BEGIN_NAMESPACE

class Ui_MainWindow
{
public:
    QWidget *centralWidget;
    QGridLayout *gridLayout_2;
    QGridLayout *gridLayout;
    QLabel *label;
    QLineEdit *friendsEdit;
    QPushButton *refreshFriendsButton;
    QLabel *label_2;
    QLineEdit *appUsersEdit;
    QPushButton *refreshAppUsersButton;
    QLabel *label_3;
    QLineEdit *messageEdit;
    QPushButton *sendMessageButton;
    QHBoxLayout *horizontalLayout;
    QProgressBar *progressBar;
    QCheckBox *integratedBox;
    QPushButton *loginButton;
    QFrame *line;
    QMenuBar *menuBar;
    QStatusBar *statusBar;

    void setupUi(QMainWindow *MainWindow)
    {
        if (MainWindow->objectName().isEmpty())
            MainWindow->setObjectName(QString::fromUtf8("MainWindow"));
        MainWindow->resize(475, 231);
        MainWindow->setUnifiedTitleAndToolBarOnMac(false);
        centralWidget = new QWidget(MainWindow);
        centralWidget->setObjectName(QString::fromUtf8("centralWidget"));
        gridLayout_2 = new QGridLayout(centralWidget);
        gridLayout_2->setSpacing(6);
        gridLayout_2->setMargin(11);
        gridLayout_2->setObjectName(QString::fromUtf8("gridLayout_2"));
        gridLayout = new QGridLayout();
        gridLayout->setSpacing(6);
        gridLayout->setObjectName(QString::fromUtf8("gridLayout"));
        label = new QLabel(centralWidget);
        label->setObjectName(QString::fromUtf8("label"));

        gridLayout->addWidget(label, 0, 0, 1, 1);

        friendsEdit = new QLineEdit(centralWidget);
        friendsEdit->setObjectName(QString::fromUtf8("friendsEdit"));
        friendsEdit->setEnabled(true);
        friendsEdit->setReadOnly(true);

        gridLayout->addWidget(friendsEdit, 0, 1, 1, 1);

        refreshFriendsButton = new QPushButton(centralWidget);
        refreshFriendsButton->setObjectName(QString::fromUtf8("refreshFriendsButton"));

        gridLayout->addWidget(refreshFriendsButton, 0, 2, 1, 1);

        label_2 = new QLabel(centralWidget);
        label_2->setObjectName(QString::fromUtf8("label_2"));

        gridLayout->addWidget(label_2, 1, 0, 1, 1);

        appUsersEdit = new QLineEdit(centralWidget);
        appUsersEdit->setObjectName(QString::fromUtf8("appUsersEdit"));
        appUsersEdit->setEnabled(true);
        appUsersEdit->setReadOnly(true);

        gridLayout->addWidget(appUsersEdit, 1, 1, 1, 1);

        refreshAppUsersButton = new QPushButton(centralWidget);
        refreshAppUsersButton->setObjectName(QString::fromUtf8("refreshAppUsersButton"));

        gridLayout->addWidget(refreshAppUsersButton, 1, 2, 1, 1);

        label_3 = new QLabel(centralWidget);
        label_3->setObjectName(QString::fromUtf8("label_3"));

        gridLayout->addWidget(label_3, 2, 0, 1, 1);

        messageEdit = new QLineEdit(centralWidget);
        messageEdit->setObjectName(QString::fromUtf8("messageEdit"));

        gridLayout->addWidget(messageEdit, 2, 1, 1, 1);

        sendMessageButton = new QPushButton(centralWidget);
        sendMessageButton->setObjectName(QString::fromUtf8("sendMessageButton"));

        gridLayout->addWidget(sendMessageButton, 2, 2, 1, 1);


        gridLayout_2->addLayout(gridLayout, 0, 0, 1, 1);

        horizontalLayout = new QHBoxLayout();
        horizontalLayout->setSpacing(6);
        horizontalLayout->setObjectName(QString::fromUtf8("horizontalLayout"));
        progressBar = new QProgressBar(centralWidget);
        progressBar->setObjectName(QString::fromUtf8("progressBar"));
        progressBar->setEnabled(true);
        progressBar->setMinimum(100);
        progressBar->setMaximum(100);
        progressBar->setValue(100);
        progressBar->setTextVisible(false);

        horizontalLayout->addWidget(progressBar);

        integratedBox = new QCheckBox(centralWidget);
        integratedBox->setObjectName(QString::fromUtf8("integratedBox"));
        integratedBox->setChecked(false);
        integratedBox->setTristate(false);

        horizontalLayout->addWidget(integratedBox);

        loginButton = new QPushButton(centralWidget);
        loginButton->setObjectName(QString::fromUtf8("loginButton"));
        loginButton->setMinimumSize(QSize(141, 0));

        horizontalLayout->addWidget(loginButton);


        gridLayout_2->addLayout(horizontalLayout, 2, 0, 1, 1);

        line = new QFrame(centralWidget);
        line->setObjectName(QString::fromUtf8("line"));
        line->setFrameShape(QFrame::HLine);
        line->setFrameShadow(QFrame::Sunken);

        gridLayout_2->addWidget(line, 1, 0, 1, 1);

        MainWindow->setCentralWidget(centralWidget);
        menuBar = new QMenuBar(MainWindow);
        menuBar->setObjectName(QString::fromUtf8("menuBar"));
        menuBar->setEnabled(true);
        menuBar->setGeometry(QRect(0, 0, 475, 22));
        MainWindow->setMenuBar(menuBar);
        statusBar = new QStatusBar(MainWindow);
        statusBar->setObjectName(QString::fromUtf8("statusBar"));
        statusBar->setAutoFillBackground(true);
        statusBar->setSizeGripEnabled(false);
        MainWindow->setStatusBar(statusBar);

        retranslateUi(MainWindow);

        QMetaObject::connectSlotsByName(MainWindow);
    } // setupUi

    void retranslateUi(QMainWindow *MainWindow)
    {
        MainWindow->setWindowTitle(QApplication::translate("MainWindow", "Gems8 Facebook Demo", 0, QApplication::UnicodeUTF8));
        label->setText(QApplication::translate("MainWindow", "Friends:", 0, QApplication::UnicodeUTF8));
        friendsEdit->setText(QApplication::translate("MainWindow", "?", 0, QApplication::UnicodeUTF8));
        refreshFriendsButton->setText(QApplication::translate("MainWindow", "Refresh", 0, QApplication::UnicodeUTF8));
        label_2->setText(QApplication::translate("MainWindow", "App users:", 0, QApplication::UnicodeUTF8));
        appUsersEdit->setText(QApplication::translate("MainWindow", "?", 0, QApplication::UnicodeUTF8));
        refreshAppUsersButton->setText(QApplication::translate("MainWindow", "Refresh", 0, QApplication::UnicodeUTF8));
        label_3->setText(QApplication::translate("MainWindow", "Message:", 0, QApplication::UnicodeUTF8));
        messageEdit->setText(QApplication::translate("MainWindow", "Message", 0, QApplication::UnicodeUTF8));
        sendMessageButton->setText(QApplication::translate("MainWindow", "Send", 0, QApplication::UnicodeUTF8));
        integratedBox->setText(QApplication::translate("MainWindow", "Use integrated browser", 0, QApplication::UnicodeUTF8));
        loginButton->setText(QApplication::translate("MainWindow", "Request permissions", 0, QApplication::UnicodeUTF8));
        Q_UNUSED(MainWindow);
    } // retranslateUi

};

namespace Ui {
    class MainWindow: public Ui_MainWindow {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_MAINWINDOW_H
