package org.irods.jargon.idrop.web.controllers

import grails.converters.JSON
import grails.rest.RestfulController

import org.irods.jargon.core.exception.CatNoAccessException
import org.irods.jargon.core.exception.JargonException
import org.irods.jargon.core.exception.NoResourceDefinedException
import org.irods.jargon.core.pub.IRODSAccessObjectFactory
import org.irods.jargon.core.pub.Stream2StreamAO
import org.irods.jargon.core.pub.io.IRODSFile
import org.irods.jargon.core.pub.io.IRODSFileFactory
import org.irods.jargon.idrop.web.services.DataProfileMidTierService
import org.irods.jargon.idrop.web.services.FileService
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

/**
 * Controller for an individual file, differentiated from the 'CollectionController' that handles listings within collections, and deals with the metadata about a collection or 
 * data object rather than the listing of children
 * @author Mike Conway - DICE 
 *
 */
class FileController extends RestfulController {

	static responseFormats = ['json']
	FileService fileService
	DataProfileMidTierService dataProfileMidTierService
	IRODSAccessObjectFactory irodsAccessObjectFactory

	/**
	 * Get the iRODS catalog info for the given path
	 */
	def index() {

		log.info("index")
		def irodsAccount = request.irodsAccount
		if (!irodsAccount) throw new IllegalStateException("no irodsAccount in request")
		def path = params.path
		log.info("path:${path}")
		def dataProfile = dataProfileMidTierService.retrieveDataProfile(path, irodsAccount)
		render dataProfile as JSON
	}


	/**
	 * POST handling file upload
	 */
	def save() {
		log.info("save()")
		log.info("upload action in file controller")
		def irodsAccount = request.irodsAccount
		if (!irodsAccount) throw new IllegalStateException("no irodsAccount in request")

		MultipartFile uploadedFile = null
		String name = ""
		String originalFileName = ""

		if (request instanceof MultipartHttpServletRequest) {
			//Get the file's name from request
			name = request.getFileNames()[0]

			log.info("name from request:${name}")
			//Get a reference to the uploaded file.
			uploadedFile = request.getFile(name)
			originalFileName = uploadedFile.originalFilename
			log.info("original filename:${originalFileName}")

		}

		//get uploaded file's inputStream
		InputStream inputStream = uploadedFile.inputStream
		//get the file storage location

		InputStream fis = new BufferedInputStream(inputStream)

		log.info("name is : ${name}")
		def irodsCollectionPath = params.collectionParentName

		if (irodsCollectionPath == null || irodsCollectionPath.empty) {
			log.error("no target iRODS collection given in upload request")
			throw new JargonException("No iRODS target collection given for upload")
		}

		try {
			IRODSFileFactory irodsFileFactory = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount)
			IRODSFile targetFile = irodsFileFactory.instanceIRODSFile(irodsCollectionPath, originalFileName)
			targetFile.setResource(irodsAccount.defaultStorageResource)

			Stream2StreamAO stream2Stream = irodsAccessObjectFactory.getStream2StreamAO(irodsAccount)
			def transferStats = stream2Stream.transferStreamToFileUsingIOStreams(fis, targetFile, 0, 1 * 1024 * 1024)

			log.info("transferStats:${transferStats}")
		} catch (NoResourceDefinedException nrd) {
			log.error("no resource defined exception", nrd)
			response.sendError(500, message(code:"message.no.resource"))
		} catch (CatNoAccessException e) {
			log.error("no access error", e)
			response.sendError(500, message(code:"message.no.access"))
		} catch (Exception e) {
			log.error("exception in upload transfer", e)
			response.sendError(500, message(code:"message.error.in.upload"))
		} finally {
			// stream2Stream will close input and output streams
		}

		render "{\"name\":\"${name}\",\"type\":\"image/jpeg\",\"size\":\"1000\"}"

	}
}