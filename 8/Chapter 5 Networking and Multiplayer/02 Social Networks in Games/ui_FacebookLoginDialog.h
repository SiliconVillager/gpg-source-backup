/********************************************************************************
** Form generated from reading ui file 'FacebookLoginDialog.ui'
**
** Created: Mon 28. Sep 14:23:59 2009
**      by: Qt User Interface Compiler version 4.5.2
**
** WARNING! All changes made in this file will be lost when recompiling ui file!
********************************************************************************/

#ifndef UI_FACEBOOKLOGINDIALOG_H
#define UI_FACEBOOKLOGINDIALOG_H

#include <QtCore/QVariant>
#include <QtGui/QAction>
#include <QtGui/QApplication>
#include <QtGui/QButtonGroup>
#include <QtGui/QDialog>
#include <QtGui/QFrame>
#include <QtGui/QHeaderView>
#include <QtGui/QPushButton>
#include <QtWebKit/QWebView>

QT_BEGIN_NAMESPACE

class Ui_FacebookLoginDialog
{
public:
    QWebView *webView;
    QFrame *line;
    QPushButton *closeButton;

    void setupUi(QDialog *FacebookLoginDialog)
    {
        if (FacebookLoginDialog->objectName().isEmpty())
            FacebookLoginDialog->setObjectName(QString::fromUtf8("FacebookLoginDialog"));
        FacebookLoginDialog->setWindowModality(Qt::NonModal);
        FacebookLoginDialog->resize(500, 496);
        FacebookLoginDialog->setSizeGripEnabled(false);
        FacebookLoginDialog->setModal(false);
        webView = new QWebView(FacebookLoginDialog);
        webView->setObjectName(QString::fromUtf8("webView"));
        webView->setGeometry(QRect(-1, -1, 500, 450));
        webView->setUrl(QUrl("about:blank"));
        line = new QFrame(FacebookLoginDialog);
        line->setObjectName(QString::fromUtf8("line"));
        line->setGeometry(QRect(0, 440, 500, 20));
        line->setFrameShape(QFrame::HLine);
        line->setFrameShadow(QFrame::Sunken);
        closeButton = new QPushButton(FacebookLoginDialog);
        closeButton->setObjectName(QString::fromUtf8("closeButton"));
        closeButton->setGeometry(QRect(380, 460, 113, 32));

        retranslateUi(FacebookLoginDialog);

        QMetaObject::connectSlotsByName(FacebookLoginDialog);
    } // setupUi

    void retranslateUi(QDialog *FacebookLoginDialog)
    {
        FacebookLoginDialog->setWindowTitle(QApplication::translate("FacebookLoginDialog", "Dialog", 0, QApplication::UnicodeUTF8));
        closeButton->setText(QApplication::translate("FacebookLoginDialog", "Close", 0, QApplication::UnicodeUTF8));
        Q_UNUSED(FacebookLoginDialog);
    } // retranslateUi

};

namespace Ui {
    class FacebookLoginDialog: public Ui_FacebookLoginDialog {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_FACEBOOKLOGINDIALOG_H
