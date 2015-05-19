package org.irods.jargon.idrop.web.services



import grails.test.mixin.*

import org.irods.jargon.core.connection.IRODSAccount
import org.irods.jargon.core.pub.CollectionAO
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO
import org.irods.jargon.core.pub.DataObjectAO
import org.irods.jargon.core.pub.IRODSAccessObjectFactory
import org.irods.jargon.core.pub.domain.ObjStat
import org.irods.jargon.core.pub.io.IRODSFile
import org.irods.jargon.core.pub.io.IRODSFileFactory
import org.irods.jargon.core.pub.io.IRODSFileInputStream
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry
import org.irods.jargon.zipservice.api.*
import org.junit.*

import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(FileService)
class FileServiceSpec extends Specification {

	void "should get obj stat"() {
		given:
		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user", "password", "", "zone", "")
		String uniqueName = "root"
		def irodsAccessObjectFactory = mockFor(IRODSAccessObjectFactory)

		ObjStat objStat = new ObjStat()

		def  listAndSearchAO = mockFor(CollectionAndDataObjectListAndSearchAO)
		listAndSearchAO.demand.retrieveObjectStatForPath{absPath -> return objStat}

		def listAndSearchAOMock = listAndSearchAO.createMock()
		irodsAccessObjectFactory.demand.getCollectionAndDataObjectListAndSearchAO{act -> return listAndSearchAOMock}

		def iafMock = irodsAccessObjectFactory.createMock()

		FileService fileService = new FileService()
		fileService.irodsAccessObjectFactory = iafMock


		when:

		def actual = fileService.retrieveObjStatForFile("blah", irodsAccount)

		then:

		actual != null
	}

	void "should retrieve object for a given path that is a collection"() {
		given:
		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user", "password", "", "zone", "")
		String uniqueName = "root"
		def irodsAccessObjectFactory = mockFor(IRODSAccessObjectFactory)

		ObjStat objStat = new ObjStat()
		objStat.objectType = CollectionAndDataObjectListingEntry.ObjectType.COLLECTION

		def  listAndSearchAO = mockFor(CollectionAndDataObjectListAndSearchAO)
		listAndSearchAO.demand.retrieveObjectStatForPath{absPath -> return objStat}

		org.irods.jargon.core.pub.domain.Collection collection = new org.irods.jargon.core.pub.domain.Collection()
		def  collectionAO = mockFor(CollectionAO)
		collectionAO.demand.findByAbsolutePath{absPath -> return collection}
		def collectionAOMock = collectionAO.createMock()


		def listAndSearchAOMock = listAndSearchAO.createMock()
		irodsAccessObjectFactory.demand.getCollectionAndDataObjectListAndSearchAO{act -> return listAndSearchAOMock}
		irodsAccessObjectFactory.demand.getCollectionAO{act -> return collectionAOMock}


		def iafMock = irodsAccessObjectFactory.createMock()

		FileService fileService = new FileService()
		fileService.irodsAccessObjectFactory = iafMock

		when:

		def actual = fileService.retrieveCatalogInfoForPath("blah", irodsAccount)

		then:

		actual != null
		actual instanceof org.irods.jargon.core.pub.domain.Collection
	}

	void "should retrieve object for a given path that is a data object"() {
		given:
		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user", "password", "", "zone", "")
		String uniqueName = "root"
		def irodsAccessObjectFactory = mockFor(IRODSAccessObjectFactory)

		ObjStat objStat = new ObjStat()
		objStat.objectType = CollectionAndDataObjectListingEntry.ObjectType.DATA_OBJECT

		def  listAndSearchAO = mockFor(CollectionAndDataObjectListAndSearchAO)
		listAndSearchAO.demand.retrieveObjectStatForPath{absPath -> return objStat}

		org.irods.jargon.core.pub.domain.DataObject dataObject = new org.irods.jargon.core.pub.domain.DataObject()
		def  dataObjectAO = mockFor(DataObjectAO)
		dataObjectAO.demand.findByAbsolutePath{absPath -> return dataObject}
		def dataObjectAOMock = dataObjectAO.createMock()


		def listAndSearchAOMock = listAndSearchAO.createMock()
		irodsAccessObjectFactory.demand.getCollectionAndDataObjectListAndSearchAO{act -> return listAndSearchAOMock}
		irodsAccessObjectFactory.demand.getDataObjectAO{act -> return dataObjectAOMock}


		def iafMock = irodsAccessObjectFactory.createMock()

		FileService fileService = new FileService()
		fileService.irodsAccessObjectFactory = iafMock

		when:

		def actual = fileService.retrieveCatalogInfoForPath("blah", irodsAccount)

		then:

		actual != null
		actual instanceof org.irods.jargon.core.pub.domain.DataObject
	}

	void "should build a single file download for a single path"() {

		given:
		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user", "password", "", "zone", "")
		String path = "/a/path/file.txt"
		def irodsAccessObjectFactory = mockFor(IRODSAccessObjectFactory)

		def inputStream = mockFor(IRODSFileInputStream)
		def inputStreamMock = inputStream.createMock()

		def irodsFile = mockFor(IRODSFile)
		irodsFile.demand.exists{-> return true}
		irodsFile.demand.canRead{-> return true}
		irodsFile.demand.length{-> return 100L}
		irodsFile.demand.getName{-> "hello"}
		irodsFile.demand.getName{-> "hello"}
		def irodsFileMock = irodsFile.createMock()

		def irodsFileFactory = mockFor(IRODSFileFactory)
		irodsFileFactory.demand.instanceIRODSFileInputStream{path1 -> return inputStreamMock}
		irodsFileFactory.demand.instanceIRODSFile{path2 -> return irodsFileMock}
		def irodsFileFactoryMock = irodsFileFactory.createMock()
		irodsAccessObjectFactory.demand.getIRODSFileFactory{acct1 -> return irodsFileFactoryMock}
		irodsAccessObjectFactory.demand.getIRODSFileFactory{acct2 -> return irodsFileFactoryMock}
		def irodsAccessObjectFactoryMock = irodsAccessObjectFactory.createMock()

		def jargonServiceFactoryService = mockFor(JargonServiceFactoryService)

		def jargonZipService = mockFor(JargonZipService)
		def jargonZipServiceMock = jargonZipService.createMock()

		jargonServiceFactoryService.demand.instanceJargonZipService{act -> return jargonZipServiceMock}
		def jargonServiceFactoryServiceMock = jargonServiceFactoryService.createMock()

		FileService fileService = new FileService()
		fileService.irodsAccessObjectFactory = irodsAccessObjectFactoryMock
		fileService.jargonServiceFactoryService = jargonServiceFactoryServiceMock

		when:

		def actual = fileService.obtainInputStreamForDownloadSingleFile(path, irodsAccount)

		then:

		actual != null
		actual instanceof DownloadFileSpecification
	}

	void "should build a multi download for multiple paths"() {

		given:

		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user", "password", "", "zone", "")
		List<String> paths = new ArrayList<String>()
		paths.add("/a/path/file.txt")
		paths.add("/a/path/file2.txt")
		def inputStream = mockFor(IRODSFileInputStream)
		def inputStreamMock = inputStream.createMock()
		def irodsAccessObjectFactory = mockFor(IRODSAccessObjectFactory)
		def irodsAccessObjectFactoryMock = irodsAccessObjectFactory.createMock()
		def jargonServiceFactoryService = mockFor(JargonServiceFactoryService)
		def length = 100L
		def bundleName = "bundle.tar"

		def jargonZipService = mockFor(JargonZipService)

		def bundleStreamWrapper = new BundleStreamWrapper(inputStreamMock, length, bundleName)
		jargonZipService.demand.obtainBundleAsInputStreamWithAdditionalMetadataGivenPaths{paths1 -> return bundleStreamWrapper}
		def jargonZipServiceMock = jargonZipService.createMock()

		jargonServiceFactoryService.demand.instanceJargonZipService{act -> return jargonZipServiceMock}
		def jargonServiceFactoryServiceMock = jargonServiceFactoryService.createMock()
		FileService fileService = new FileService()
		fileService.irodsAccessObjectFactory = irodsAccessObjectFactoryMock
		fileService.jargonServiceFactoryService = jargonServiceFactoryServiceMock

		when:

		def actual = fileService.obtainInputStreamForDownloadMultipleFiles(paths, irodsAccount)

		then:

		actual != null
		actual instanceof DownloadFileSpecification
		actual.length == length
		actual.bundleFileName == bundleName
		actual.inputStream != null
	}
}
