//         Facebook Demo for Game Programming Gems 8 by Claus Höfele          //
//==============================================================================

#include "FacebookCreateToken.h"

#include <QDomDocument>

//------------------------------------------------------------------------------
/*static*/ const QString FacebookCreateTokenRequest::requestName("facebook.auth.createToken");

//------------------------------------------------------------------------------
FacebookServiceState FacebookCreateTokenRequest::operator()(const Input& /*input*/,
  QVector<QString>& /*parameters*/)
{
  // No input parameters.
  return FacebookServiceStates::Ok;
}

//------------------------------------------------------------------------------
FacebookServiceState FacebookCreateTokenResponse::operator()(
  const QDomDocument& document, Output& output)
{
  QDomNodeList uid = document.elementsByTagName("auth_createToken_response");
  output = uid.at(0).toElement().text();

  return FacebookServiceStates::Ok;
}
