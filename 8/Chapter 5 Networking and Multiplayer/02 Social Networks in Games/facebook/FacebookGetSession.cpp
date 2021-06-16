//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#include "FacebookGetSession.h"

#include <QDomDocument>
#include <QVector>

//------------------------------------------------------------------------------
/*static*/ const QString FacebookGetSessionRequest::requestName("facebook.auth.getSession");

//------------------------------------------------------------------------------
FacebookServiceState FacebookGetSessionRequest::operator()(const Input& input,
  QVector<QString>& parameters)
{
  parameters.append(QString("auth_token=%1").arg(input));
  return FacebookServiceStates::Ok;
}

//------------------------------------------------------------------------------
FacebookServiceState FacebookGetSessionResponse::operator()(
  const QDomDocument& document, Output& output)
{
  QDomNodeList uid = document.elementsByTagName("auth_getSession_response");
  QDomNode response = uid.at(0);
  output.sessionKey = response.firstChildElement("session_key").toElement().text();;
  output.uid = response.firstChildElement("uid").toElement().text();
  output.sessionSecret = response.firstChildElement("secret").toElement().text();
  return FacebookServiceStates::Ok;
}
